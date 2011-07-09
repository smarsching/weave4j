/*
 * weave4j - Weave Server for Java
 * Copyright (C) 2011  Sebastian Marsching
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marsching.weave4j.web;

/**
 * Stores captcha configuration.
 * 
 * @author Sebastian Marsching
 *
 */
public class CaptchaSettings {

    private boolean enableCaptchas;

    private String recaptchaPrivateKey;

    private String recaptchaPublicKey;

    /**
     * If <code>true</code>, users wanting to register or reset their password
     * have to solve a captcha first. If this option is enabled, the reCAPTCHA
     * private and public key have to be set as well.
     * 
     * @return <code>true</code> if captchas are enabled, <code>false</code> 
     *  otherwise
     */
    public boolean isEnableCaptchas() {
        return enableCaptchas;
    }

    /**
     * Enable or disable captchas. If <code>true</code>, users wanting to
     * register or reset their password have to solve a captcha first. If this 
     * option is enabled, the reCAPTCHA private and public key have to be set 
     * as well.
     * 
     * @param enableCaptchas <code>true</code> to enable, <code>false</code> to
     *  disable captchas
     */
    public void setEnableCaptchas(boolean enableCaptchas) {
        this.enableCaptchas = enableCaptchas;
    }

    /**
     * Returns the reCAPTCHA private key.
     * 
     * @return reCAPTCHA private key
     */
    public String getRecaptchaPrivateKey() {
        return recaptchaPrivateKey;
    }

    /**
     * Sets the reCAPTCHA private key.
     * 
     * @param recaptchaPrivateKey reCAPTCHA private key
     */
    public void setRecaptchaPrivateKey(String recaptchaPrivateKey) {
        this.recaptchaPrivateKey = recaptchaPrivateKey;
    }

    /**
     * Returns the reCAPTCHA public key.
     * 
     * @return reCAPTCHA public key
     */
    public String getRecaptchaPublicKey() {
        return recaptchaPublicKey;
    }

    /**
     * Sets the reCAPTCHA public key.
     * 
     * @param recaptchaPublicKey reCAPTCHA public key
     */
    public void setRecaptchaPublicKey(String recaptchaPublicKey) {
        this.recaptchaPublicKey = recaptchaPublicKey;
    }

}
