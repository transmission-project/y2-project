package com.example.huntertalk;

import android.support.annotation.Nullable;

 class RegistrationFormState {


        @Nullable
        private Integer usernameError;
        @Nullable
        private Integer passwordError;
       // private Integer confirmpasswprdError;
        private boolean isDataValid;

        RegistrationFormState(@Nullable Integer usernameError, @Nullable Integer passwordError) {
            this.usernameError = usernameError;
            this.passwordError = passwordError;
            this.isDataValid = false;
        }

        RegistrationFormState(boolean isDataValid) {
            this.usernameError = null;
            this.passwordError = null;
            this.isDataValid = isDataValid;
        }

        @Nullable
        Integer getUsernameError() {
            return usernameError;
        }

        @Nullable
        Integer getPasswordError() {
            return passwordError;
        }

        boolean isDataValid() {
            return isDataValid;
        }
    }


