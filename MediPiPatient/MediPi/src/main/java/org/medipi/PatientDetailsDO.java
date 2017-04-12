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
package org.medipi;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.medipi.utilities.Utilities;

/**
 * Class to contain the Patient Details and has methods to verify aspects of the
 * patient data
 *
 * @author rick@robinsonhq.com
 */
public class PatientDetailsDO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String nhsNumber;
    private String forename;
    private String surname;
    private String dob;

    /**
     * Constructor
     */
    public PatientDetailsDO() {
    }

    public PatientDetailsDO(String nhsNumber, String forename, String surname, String dob) throws Exception {
        this.nhsNumber = nhsNumber;
        this.forename = forename;
        this.surname = surname;
        this.dob = dob;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDob() throws Exception {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (nhsNumber != null ? nhsNumber.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PatientDetailsDO)) {
            return false;
        }
        PatientDetailsDO other = (PatientDetailsDO) object;
        if ((this.nhsNumber == null && other.nhsNumber != null) || (this.nhsNumber != null && !this.nhsNumber.equals(other.nhsNumber))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.medipi.PatientDetailsDO [ nhsNumber=" + nhsNumber + " ]";
    }

    public boolean checkValidity() throws Exception {
        StringBuilder sb = new StringBuilder();
        if (forename == null || forename.trim().equals("")) {
            sb.append(" Patient First Name is not set.");
        }
        try {
            nameCheck(forename);
        } catch (Exception e) {
            sb.append(e.getLocalizedMessage());
        }
        if (surname == null || surname.trim().equals("")) {
            sb.append(" Patient Surname is not set.");
        }
        try {
            nameCheck(surname);
        } catch (Exception e) {
            sb.append(e.getLocalizedMessage());
        }
        if (nhsNumber == null || nhsNumber.trim().equals("")) {
            sb.append(" Patient NHS Number is not set.");
        } else {
            // Check the NHS Number is valid
            //LENGTH CHECK
            if (nhsNumber.length() != 10) {
                sb.append(" Patient NHS Number (").append(nhsNumber).append(") is not the correct length.");
            }

            // NUMERIC CHECK
            Long i;
            try {
                i = Long.parseLong(nhsNumber);
            } catch (NumberFormatException e) {
                sb.append(" Patient NHS Number(").append(nhsNumber).append(") contains non numeric content.");
            }
            // MOD11 CHECK
            int len = nhsNumber.length();
            int sum = 0;
            for (int k = 1; k <= len; k++) // compute weighted sum
            {
                sum += (11 - k) * Character.getNumericValue(nhsNumber.charAt(k - 1));
            }
            if ((sum % 11) != 0) {
                sb.append(" Patient NHS Number(").append(nhsNumber).append(") checksum is not correct.");
            }
            if (dob == null || dob.trim().equals("")) {
                sb.append(" Patient DOB is not set.");
            } else {
                try {
                    formatDOB(dob);
                } catch (Exception e) {
                    sb.append(e.getLocalizedMessage());
                }
            }

        }
        if (sb.length() == 0) {
            return true;
        } else {
            throw new Exception(sb.toString());
        }

    }

    public void nameCheck(String name) throws Exception {
        boolean isChars = name.matches("^[A-Za-z]+$");
        boolean isRightLength = name.length() <= 35 && name.length() >= 2;
        if (!isChars) {
            throw new Exception(" The forename/surname fields must contain only characters.");
        }
        if (!isRightLength) {
            throw new Exception(" The forename/surname fields must be 2 or more,but fewer than 35 characters long.", null);
        }

    }

    public String formatDOB(String preformat) throws Exception {
        try {
            LocalDate dobld = LocalDate.parse(preformat, DateTimeFormatter.BASIC_ISO_DATE);
            if(dobld.isAfter(LocalDate.now())){
                throw new Exception("The DOB is in the future");
            }
            return dobld.format(Utilities.DISPLAY_DOB_FORMAT);
        } catch (DateTimeParseException e) {
            throw new Exception(e.getLocalizedMessage());
        }
    }
        public String formatNHSNumber(String preformat) throws Exception {
        return preformat.substring(0,3).concat("-").concat(preformat.substring(3,6)).concat("-").concat(preformat.substring(6,10));
    }

}
