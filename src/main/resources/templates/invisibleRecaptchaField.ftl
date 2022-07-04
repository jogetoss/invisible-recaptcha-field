<#--  show if in form builder  -->
<#--  includeMetaData will be true if in form builder -->
<#if includeMetaData>
  <div class="form-cell" ${elementMetaData!}>
    <label class="label">${element.properties.id}</label>
    <span class="form-floating-label">Invisible reCAPTCHA</span>
  </div>

<#--  show if in userview  -->
<#else>
<div class="invisibleRecaptchaField">
  <input id="${elementParamName!}" name="${elementParamName!}" type="hidden" value="${value!?html}"/>
  <#-- show if customParameterName not exist -->
  <#-- if customParameterName exist, is in subform, so don't run -->
  <#if !element.properties.customParameterName??>
    <script>
      let grecaptchaPluginButtonRendered = false;
      let grecaptchaWidgetId_${element.properties.elementUniqueKey!} = "";

      async function executeGrecaptcha() {
        grecaptcha.execute(grecaptchaWidgetId_${element.properties.elementUniqueKey!});
        let promise = new Promise((resolve, reject) => {
          const interval = setInterval(function () {
            let response = grecaptcha.getResponse(grecaptchaWidgetId_${element.properties.elementUniqueKey!});
            if (response !== '') {
              resolve(response);
              clearInterval(interval);
            }
          }, 100);
        });
        return promise;
      }

      var setupGrecaptchaEvent = function () {
        /**
         * Renders a new save button to trigger reCAPTCHA
         * and hides original form submit button
         * only if no button is previously rendered
         **/
        if (!grecaptchaPluginButtonRendered) {
          const originalSubmitButton = $('div#section-actions').find('input[type="submit"]');
          const submitFormCell = $('.form-cell').has('input[type="submit"]');
          let newGrecaptchaFormCell = submitFormCell.clone();

          newGrecaptchaFormCell.find('input[type="submit"]')
                  .attr('id', 'recaptcha-button-${element.properties.elementUniqueKey!}')
                  .attr('name', 'recaptcha-button-${element.properties.elementUniqueKey!}')
                  .attr('type', 'button');

          $('div#section-actions .form-column').append(newGrecaptchaFormCell);
          submitFormCell.hide();

          // Only bind a click event onto the specified button
          $('#recaptcha-button-${element.properties.elementUniqueKey!}').on('click', async function (e) {
            e.preventDefault();
            let token = await executeGrecaptcha();
            FormUtil.getField('${elementParamName!}').val(token);
            originalSubmitButton.click();
          });

          // Only bind a enter keypress event onto the form
          $('form').on('keypress', async function (e) {
            if (e.which === 13) {
              e.preventDefault();
              $('#recaptcha-button-${element.properties.elementUniqueKey!}').click();
              return false;
            }
          });

          // Finally, render the reCAPTCHA widget to the form
          grecaptchaWidgetId_${element.properties.elementUniqueKey!}
                  = grecaptcha.render('g-recaptcha-${element.properties.elementUniqueKey!}', {
                      'sitekey': '${siteKey}',
                      'size': 'invisible'
                    });

          // Set flag
          grecaptchaPluginButtonRendered = true;
        }
      };
    </script>
    <div id="g-recaptcha-${element.properties.elementUniqueKey!}"></div>
    <script src="https://www.google.com/recaptcha/api.js?onload=setupGrecaptchaEvent&render=explicit" async defer></script>
  </#if>
</div>
</#if>