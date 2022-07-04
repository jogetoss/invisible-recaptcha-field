package org.joget.marketplace;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;

/**
 * Adds an invisible Google reCAPTCHA v2 as a form element.
 * This plugin creates a new button that triggers the reCAPTCHA, hides the 
 * original submit button of the form, and programmatically triggers the button.
 */
public class InvisibleRecaptchaField extends Element implements FormBuilderPaletteElement {
    
    private final static String MESSAGE_PATH = "message/form/invisibleRecaptchaField";
    
    @Override
    public String getName() {
        return AppPluginUtil.getMessage("org.joget.marketplace.InvisibleRecaptchaField.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getVersion() {
        return AppPluginUtil.getMessage("org.joget.marketplace.InvisibleRecaptchaField.pluginVer", getClassName(), MESSAGE_PATH);
    }
    
    @Override
    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    public String getFormBuilderCategory() {
        return AppPluginUtil.getMessage("org.joget.marketplace.InvisibleRecaptchaField.pluginCategory", getClassName(), MESSAGE_PATH);
    }
    
    @Override
    public String getLabel() {
        return AppPluginUtil.getMessage("org.joget.marketplace.InvisibleRecaptchaField.pluginLabel", getClassName(), MESSAGE_PATH);
    }
    
    @Override
    public String getDescription() {
        return AppPluginUtil.getMessage("org.joget.marketplace.InvisibleRecaptchaField.pluginDesc", getClassName(), MESSAGE_PATH);
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/form/invisibleRecaptchaField.json", null, true, MESSAGE_PATH);
    }
    
    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + getLabel() + "</label>";
    }

    @Override
    public int getFormBuilderPosition() {
        return 100;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fa fa-google\"></i>";
    }

    /**
     * Validates Google reCAPTCHA v2.<br>
     * Code referenced from 
     * <a href="https://www.baeldung.com/gson-deserialization-guide">Baeldung Gson Deserialization</a>
     * and <a href="https://www.baeldung.com/java-9-http-client">Baeldung Java HttpClient</a>.
     *
     * @param response reCAPTCHA response from client side.
     * (g-recaptcha-response)
     * @param secret Secret key (key given for communication between your
     * verification backend and Google)
     * @return true if validation successful, false otherwise.
     */
    public GoogleRecaptchaResponse recaptchaVerify(String response, String secret) {
        Gson gson = new Gson();
        GoogleRecaptchaResponse gr = null;
        String postParams = "response=" + response + "&secret=" + secret;
        
        // Create HttpClient and HttpRequest
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.google.com/recaptcha/api/siteverify"))
                .headers("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(postParams))
                .build();

        try {
            // Send request to server and get response
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Map JSON response to GoogleRecaptchaResponse class
            gr = gson.fromJson(httpResponse.body(), GoogleRecaptchaResponse.class);

        } catch (IOException | InterruptedException e) {
            LogUtil.error(getClassName(), e, "recaptchaVerify error");
        }
        return gr;
    }
    
    @Override
    public Boolean selfValidate(FormData formData) {
        
        // Has value if element is in subform
        // If element is in subform, we return true (no checking)
        String customParameterName = getPropertyString("customParameterName");
        if (!customParameterName.isEmpty()) return true;
        
        // Get token submitted from client
        String value = FormUtil.getElementPropertyValue(this, formData);

        // Get secret key from element properties
        String secretKey = getPropertyString("secretKey");

        // Initiate recaptcha verify
        GoogleRecaptchaResponse recaptcha = recaptchaVerify(value, secretKey);
        
        // Check if "Debug mode" is enabled
        String debugModeBoolean = getPropertyString("debugMode");
        if ("enabled".equalsIgnoreCase(debugModeBoolean)) {
            // Log all plugin's properties and value
            LogUtil.info(getClassName(), "===== DEBUGGING " + getLabel() + " =====");
            LogUtil.info(getClassName(), "ID: " + getPropertyString("id"));
            LogUtil.info(getClassName(), "siteKey: " + getPropertyString("siteKey"));
            LogUtil.info(getClassName(), "secretKey: " + secretKey);
            LogUtil.info(getClassName(), "customError: " + getPropertyString("customError"));
            LogUtil.info(getClassName(), "debugMode: " + debugModeBoolean);
            LogUtil.info(getClassName(), "value: " + value);
            LogUtil.info(getClassName(), "reCAPTCHA status: " + recaptcha.isSuccess());
            LogUtil.info(getClassName(), "reCAPTCHA error: " + (recaptcha.isSuccess() ? "null" : recaptcha.getErrorCodes().toString()));
            LogUtil.info(getClassName(), "===== END =====");
        }
        
        // If errors, validation error and show errors in client
        if (!recaptcha.isSuccess()) {
            Form rootForm = FormUtil.findRootForm(this);
            String rootFormId = FormUtil.getElementParameterName(rootForm);

            // check if debugMode is enabled
            if ("enabled".equalsIgnoreCase(debugModeBoolean)) {
                // Print verbose error in form
                formData.addFormError(rootFormId, "[reCAPTCHA DEBUG MODE ENABLED!]");
                formData.addFormError(rootFormId, "reCAPTCHA error: " + recaptcha.getErrorCodes().toString());
            } else {
                // If debug mode disabled, print custom error
                String customError = getPropertyString("customError");
                formData.addFormError(rootFormId, customError.isEmpty() ? "reCAPTCHA error" : customError);
            }
            return false;
        }
        return true;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        /**
         * Submitted recaptcha token is one time use only,
         * does not seem to be useful in recurring context
         * however if required, can be included below
         */
        // String value = FormUtil.getElementPropertyValue(this, formData);
        // dataModel.put("value", value);
        
        // Get sitekey value and store into data model to be used in the client
        String siteKey = getPropertyString("siteKey");
        dataModel.put("siteKey", siteKey);
        
        // Generate and return the HTML to be shown in the client
        String template = "invisibleRecaptchaField.ftl";
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }
}
