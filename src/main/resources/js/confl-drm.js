(function($) {  // this closure helps us keep our variables to ourselves
// This pattern is known as an "iife" - an immediately invoked function expression.
    // form the URL
    var url = AJS.contextPath() + "/rest/drmsetting/1.0/";

    function updateConfig() {
        AJS.$.ajax({
            url: url,
            type: "PUT",
            contentType: "application/json",
            data: '{ "isUploadEnable" :"' + AJS.$("#isUploadEnable").prop("checked") + '",'
                + ' "isDownloadEnable" :"' + AJS.$("#isDownloadEnable").prop("checked") + '",'
                + ' "extension" :"' + AJS.$("#extension").attr("value") + '",'
                + ' "properties" :"' + AJS.$("#properties").attr("value") + '",'
                + ' "system" :"' + AJS.$("#system").attr("value") + '",'
                + ' "allowExtension" :"' + AJS.$("#allowExtension").attr("value") + '",'
                + ' "prohibitExtension" :"' + AJS.$("#prohibitExtension").attr("value") + '",'
                + ' "keyFile" :"' + AJS.$("#keyFile").attr("value") + '",'
                + ' "userId" :"' + AJS.$("#userId").attr("value") + '",'
                + ' "auth" :"' + AJS.$("#auth").attr("value") + '"'
                + '}',
            processData: false,
            success: function (data, status, xhr) {
                alert('Successfully Applied!');
            }
        });
    }

    function validateInput() {
        if (AJS.$("extension").attr("value") == '') return false;
        if (AJS.$("properties").attr("value") == '') return false;
        if (AJS.$("system").attr("value") == '') return false;
        if (AJS.$("allowExtension").attr("value") == '') return false;
        if (AJS.$("prohibitExtension").attr("value") == '') return false;
        if (AJS.$("keyFile").attr("value") == '') return false;
        if (AJS.$("userId").attr("value") == '') return false;
        if (AJS.$("auth").attr("value") == '') return false;

        return true;
    }

    // wait for the DOM (i.e., document "skeleton") to load. This likely isn't
    // necessary for the current case,
    // but may be helpful for AJAX that provides secondary content.
    $(document).ready(function () {
        // request the config information from the server
        $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
        }).done(function (config) {  // when the configuration is returned...
            // ...populate the form.
            if (config.isUploadEnable == "true") {
                $("#isUploadEnable").prop("checked", config.isUploadEnable);
            } else {
                $("#isUploadEnable").prop("checked", "");
            }
            if (config.isDownloadEnable == "true") {
                $("#isDownloadEnable").prop("checked", config.isDownloadEnable);
            } else {
                $("#isDownloadEnable").prop("checked", "");
            }
            $("#extension").val(config.extension);
            $("#properties").val(config.properties);
            $("#system").val(config.system);
            $("#allowExtension").val(config.allowExtension);
            $("#prohibitExtension").val(config.prohibitExtension);
            $("#keyFile").val(config.keyFile);
            $("#userId").val(config.userId);
            $("#auth").val(config.auth);

            AJS.$("#admin").submit(function (event) {
                event.preventDefault();
                updateConfig();
            });
        });

        $('input[type="checkbox"][name="isUploadEnable"]').change(function () {
            if ($("#isUploadEnable").prop("checked")) {

                if (validateInput() == false) {
                    this.checked = false;
                    alert('파라미터 값을 입력하여 주시기 바랍니다.');
                    return;
                }
            }
        });
        $('input[type="checkbox"][name="isDownloadEnable"]').change(function () {
            if ($("#isDownloadEnable").prop("checked")) {

                if (validateInput() == false) {
                    this.checked = false;
                    alert('파라미터 값을 입력하여 주시기 바랍니다.');
                    return;
                }
            }
        });

    });

})(AJS.$ || jQuery);