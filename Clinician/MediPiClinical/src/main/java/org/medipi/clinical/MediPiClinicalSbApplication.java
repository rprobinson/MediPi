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
package org.medipi.clinical;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import org.medipi.clinical.logging.MediPiLogger;
import org.medipi.clinical.services.SSLClientHttpRequestFactory;
import org.medipi.clinical.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

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
@EnableScheduling
public class MediPiClinicalSbApplication implements CommandLineRunner {

    private static final String LOG = "medipi.log";
    // MediPi version Number
    private static final String MEDIPINAME = "MediPi Clinical";
    private static final String VERSION = "MediPiClinical_v1.0.5";
    private static final String VERSIONNAME = "PILOT-20170921-1";
    private final MediPiLogger logger = MediPiLogger.getInstance();

    @Autowired
    private SSLClientHttpRequestFactory requestFactory;
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
        // initialise utilities which is accessible to all classes which have reference to MediPi.class
        Utilities utils = Utilities.getInstance();
        utils.setProperties(properties);

        //Set up logging
        String log = properties.getProperty(LOG);
        if (log == null || log.trim().equals("")) {
            System.out.println(log + " - MediPi log directory is not set");
            System.exit(1);
        } else if (new File(log).isDirectory()) {
            logger.setAppName("MEDIPI", log);
            logger.log(MediPiClinicalSbApplication.class.getName() + "startup", versionIdent);
        } else {
            System.out.println("FATAL: " + log + " - MediPi log directory is not a directory");
            System.exit(1);
        }

        requestFactory = new SSLClientHttpRequestFactory();

        System.out.println("ServletContextListener started");

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
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(MediPiClinicalSbApplication.class, args);
    }

}
