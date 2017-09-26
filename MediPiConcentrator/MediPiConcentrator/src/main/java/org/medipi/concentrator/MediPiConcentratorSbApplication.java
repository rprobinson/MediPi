/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.concentrator;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import javax.servlet.ServletContext;
import org.medipi.security.CertificateDefinitions;
import org.medipi.concentrator.dataformat.DataFormatFactory;
import org.medipi.concentrator.dataformat.PatientUploadDataFormat;
import org.medipi.security.UploadEncryptionAdapter;
import org.medipi.concentrator.logging.MediPiLogger;
import org.medipi.concentrator.utilities.ConfigurationStringTokeniser;
import org.medipi.concentrator.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Class bootstrap the Spring Boot Application
 *
 * This class calls all the configuration necessary to boot the concentrator It
 * should be called using one argument - location of the MediPiConcentrator
 * properties file
 *
 * @author rick@robinsonhq.com
 */
@SpringBootApplication
@EnableAutoConfiguration
@Configuration
@ComponentScan
public class MediPiConcentratorSbApplication implements CommandLineRunner {

    // MediPi version Number
    private static final String MEDIPINAME = "MediPi Concentrator";
    private static final String VERSION = "MediPiConcentrator_v1.0.6";
    private static final String VERSIONNAME = "PILOT-20170921-1";
    private final MediPiLogger logger = MediPiLogger.getInstance();
    private ServletContext scxt;
    private final UploadEncryptionAdapter patientEncryptionAdapter = new UploadEncryptionAdapter();
    private final UploadEncryptionAdapter clinicianEncryptionAdapter = new UploadEncryptionAdapter();

    PatientUploadDataFormat format;
    
    @Value("${medipi.log}")
    private String log;

    @Autowired
    DataFormatFactory dff;

    /**
     * Run method inherited by the commandLineRunner. This method sets the
     * version, calls the properties and utilities classes and instantiates any
     * formats used in transaction of data from MediPi Patient units to the
     * Concentrator
     *
     * @param args from the call of the Spring Boot Application
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        String prop = null;
        if (args.length != 2) {
            System.exit(1);
        } else {
        prop = args[0];
        }        // set versioning and print to standard output
        String versionIdent = MEDIPINAME + " " + VERSION + "-" + VERSIONNAME + " starting at " + new Date();
        System.out.println(versionIdent);

        MediPiProperties mpp = MediPiProperties.getInstance();

        if (!mpp.setProperties(prop)) {
            System.out.println("FATAL: Properties file failed to load");
            System.exit(1);
        }

        Properties properties = mpp.getProperties();
        // initialise utilities which is accessible to all classes 
        Utilities utils = Utilities.getInstance();
        utils.setProperties(properties);

        //Set up logging
        if (log == null || log.trim().equals("")) {
            System.out.println(log + " - MediPi log directory is not set");
            System.exit(1);
        } else if (new File(log).isDirectory()) {
            logger.setAppName("MEDIPI", log);
            logger.log(MediPiConcentratorSbApplication.class.getName() + "startup", versionIdent);
        } else {
            System.out.println("FATAL: " + log + " - MediPi log directory is not a directory");
            System.exit(1);
        }

        // instantiate the patient encryption adapter
        CertificateDefinitions patientCD = new CertificateDefinitions(utils.getProperties());
        String patientAdapterError = patientEncryptionAdapter.init(patientCD, UploadEncryptionAdapter.SERVERMODE);
        if (patientAdapterError != null) {
            logger.log(MediPiConcentratorSbApplication.class.getName() + ".error", "Failed to instantiate Patient Encryption Adapter: " + patientAdapterError);
            System.out.println("Failed to instantiate Patient Encryption Adapter: " + patientAdapterError);
        }
        // instantiate the clinician encryption adapter
        CertificateDefinitions clinicianCD = new CertificateDefinitions(utils.getProperties());
        clinicianCD.setSIGNTRUSTSTORELOCATION("medipi.json.sign.truststore.clinician.location", CertificateDefinitions.INTERNAL);
        clinicianCD.setSIGNTRUSTSTOREPASSWORD("medipi.json.sign.truststore.clinician.password", CertificateDefinitions.INTERNAL);
        clinicianCD.setENCRYPTKEYSTORELOCATION("medipi.json.encrypt.keystore.clinician.location", CertificateDefinitions.INTERNAL);
        clinicianCD.setENCRYPTKEYSTOREALIAS("medipi.json.encrypt.keystore.clinician.alias", CertificateDefinitions.INTERNAL);
        clinicianCD.setENCRYPTKEYSTOREPASSWORD("medipi.json.encrypt.keystore.clinician.password", CertificateDefinitions.INTERNAL);

        String ClinicicanAdapterError = clinicianEncryptionAdapter.init(clinicianCD, UploadEncryptionAdapter.SERVERMODE);
        if (ClinicicanAdapterError != null) {
            logger.log(MediPiConcentratorSbApplication.class.getName() + ".error", "Failed to instantiate Clinician Encryption Adapter: " + ClinicicanAdapterError);
            System.out.println("Failed to instantiate Clinician Encryption Adapter: " + ClinicicanAdapterError);
        }

        try {
            // loop through all the data format class tokens defined in the properties file and instantiate
            String e = properties.getProperty("medipi.concentrator.dataformatclasstokens");
            if (e != null && e.trim().length() != 0) {
                ConfigurationStringTokeniser cst = new ConfigurationStringTokeniser(e);
                while (cst.hasMoreTokens()) {
                    String classToken = cst.nextToken();
                    String dataFormatClass = properties.getProperty("medipi.concentrator.dataformat." + classToken + ".class");
                    try {
                        format = dff.getDataFormatClass(dataFormatClass);
                        format.setClassToken(classToken);
                        String initError = format.init();
                        if (initError == null) {
                            scxt.setAttribute(classToken, format);
                        } else {
                            System.out.println("FATAL: Cannot instantiate a data format: " + classToken + " - " + dataFormatClass + " - " + initError);
                            logger.log(MediPiConcentratorSbApplication.class.getName() + ".fatal", "FATAL: Cannot instantiate a data format: \n" + classToken + " - " + dataFormatClass + " - " + initError);
                            System.exit(1);
                        }
                    } catch (Exception ex) {
                        System.out.println("FATAL: Cannot instantiate a data format: " + classToken + " - " + dataFormatClass + " - " + ex.getMessage());
                        logger.log(MediPiConcentratorSbApplication.class.getName() + ".fatal", "FATAL: Cannot instantiate a data format: \n" + classToken + " - " + dataFormatClass + " - " + ex.getMessage());
                        System.exit(1);
                    }
                }
            } else {
                System.out.println("FATAL: No data formats have been defined");
                logger.log(MediPiConcentratorSbApplication.class.getName() + ".fatal", "FATAL: No data formats have been defined");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("FATAL: Failed to tokenise the data format class token list- " + e.getMessage());
            logger.log(MediPiConcentratorSbApplication.class.getName() + ".fatal", "FATAL: Failed to tokenise the data format class token list- " + e.getMessage());
            System.exit(1);
        }
        System.out.println("ServletContextListener started");

    }

    /**
     * Bean to access the ServletContext for use in the configuration run method
     *
     * @return
     */
    @Bean
    public ServletContextInitializer initializer() {
        return (ServletContext servletContext) -> {
            scxt = servletContext;
        };
    }

    /**
     * Bean to make available the MediPiLogger object
     *
     * @return instance of MediPiLogger singleton
     */
    @Bean
    public MediPiLogger mediPiLogger() {
        return MediPiLogger.getInstance();
    }

    /**
     * Bean to make available the utils object
     *
     * @return instance of utils singleton
     */
    @Bean
    public Utilities utils() {
        return Utilities.getInstance();
    }

    /**
     * Bean to make available the patient encryption adaptor object
     *
     * @return instance of patient encryption adaptor singleton
     */
    @Bean
    public UploadEncryptionAdapter patientEncryptionAdapter() {
        return patientEncryptionAdapter;
    }

    /**
     * Bean to make available the clinician encryption adaptor object
     *
     * @return instance of clinician encryption adaptor singleton
     */
    @Bean
    public UploadEncryptionAdapter clinicianEncryptionAdapter() {
        return clinicianEncryptionAdapter;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(MediPiConcentratorSbApplication.class, args);
    }

}
