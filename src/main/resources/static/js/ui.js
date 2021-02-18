// Let's start with some global state for convenience

var currentInputFormat = "";
var currentOutputFormat = "";
var currentInputMode = "";
var currentInputFile = "";
var currentInputText = 0; // 0/1 for empty/non-empty
var binaryInput = false;
var binaryOutput = false;

function registerListeners() {

$("#input-format").on("change", function() {
  var inputFormat = $(this).val();
  if (currentInputFormat != inputFormat) {
    currentInputFormat = inputFormat;
    // Otherwise simple but need to disable text-input for binary input
    binaryInput = isBinary(inputFormat);
    if (binaryInput) {
      $("#input-mode").val("input-mode-file").prop("disabled", true);
      // will not fire event so update directly:
      currentInputMode = "input-mode-file";
    } else {
      $("#input-mode").prop("disabled", false);
    }
    updateUIState();
  }
});
$("#output-format").on("change", function() {
  var outputFormat = $(this).val();
  if (currentOutputFormat != outputFormat) {
    currentOutputFormat = outputFormat;
    // Otherwise simple but need to disable "transform & show" option
    binaryOutput = isBinary(outputFormat);
    if (binaryOutput) {
      $("#button-show").addClass("hidden");
    } else {
      $("#button-show").removeClass("hidden");
    }
    updateUIState();
  }
});
$("#input-mode").on("change", function() {
  var inputMode = $(this).val();
  if (currentInputMode != inputMode) {
    currentInputMode = inputMode;
    updateUIState();
  }
});
$("#input-as-file").on("change", function() {
  var inputFile = $(this).val();
  if (currentInputFile != inputFile) {
    currentInputFile = inputFile;
    updateUIState();
  }
});
// Textareas are tricky. 'Change' only triggers on losing focus so need
// to also do "keyup" it seems
var inputAsText = $("#input-as-text");
var inputAsTextFunc = function() {
  var inputStr = $(this).val();
  var inputText = Math.min(inputStr.length, 1);
  if (currentInputText != inputText) {
    currentInputText = inputText;
    updateUIState();
  }
};
inputAsText.on("change", inputAsTextFunc);
inputAsText.on("keyup", inputAsTextFunc);

// Second: invoking transformations: one is longer via ajax; another simple submit
$("#button-show").click(actionTransformAndShow);
$("#button-download").click(function() {
  $("#form-input").submit();
});

}

function updateUIState() {
  if (currentInputFormat) { // got valid input format choice
    showRow("input-format");
    if (currentOutputFormat) { // got valid output format choice
      showRow("output-format");
      var result = updateUIStateByInputMode();
      if (result) { // got input content select!
        activateRow("action");
      } else { // no input mode
        hideRow("action");
      }
    } else {
      activateRow("output-format");
      hideRow("input-mode");
      hideRow("input-as-file");
      hideRow("input-as-text");
      hideRow("action-row");
    }
  } else { // no valid choice
      activateRow("input-format");
      hideRow("output-format");
      hideRow("input-mode");
      hideRow("input-as-file");
      hideRow("input-as-text");
      hideRow("action");
  }
}

function updateUIStateByInputMode() {
  if (currentInputMode == "input-mode-file") {
    showRow("input-mode");
    activateRow("input-as-file");
    hideRow("input-as-text");
    return currentInputFile;
  }
  if (currentInputMode == "input-mode-text") {
    showRow("input-mode");
    hideRow("input-as-file");
    activateRow("input-as-text");
    return currentInputText;
  }
  // No valid selection
  activateRow("input-mode");
  hideRow("input-as-file");
  hideRow("input-as-text");
  return false;
}

function isBinary(formatId) {
  switch (formatId) {
  case "bson":
  case "cbor":
  case "ion-binary":
  case "msgpack":
  case "smile":
  	return true;
  }
  return false;
}

function hideRow(rowId) {
  $("#"+rowId+"-row").removeClass("shown active").addClass("hidden");
}

function showRow(rowId) {
  $("#"+rowId+"-row").removeClass("hidden active").addClass("shown");
}

function activateRow(idBase) {
  // activate row via class; put focus on selector
  $("#"+idBase+"-row").removeClass("hidden shown").addClass("active");
  $("#"+idBase).focus();
}

function actionTransformAndShow() {
  // First: which mode is user expecting? Submission methods vary a lot
  if (currentInputMode == "input-mode-file") {
    actionTransformFromInputFile();
  } else if (currentInputMode == "input-mode-text") {
    actionTransformFromInputText();
  } else {
    alert("Jackformer is confused: unrecognized current input mode of: "+currentInputMode);
  }
}

function actionTransformFromInputFile() {
  // clean output first:
  $("#output-content").val("");
  // then try to submit
  var form = $("#form-input");
  var formData = new FormData(form[0]);

  $.ajax({
        url: "rest/jackform-input-file",
        type: "POST",
        cache: false,
        // Sending content as form data
        data: formData,
// Not sure why but as per:
// https://stackoverflow.com/questions/25390598/append-called-on-an-object-that-does-not-implement-interface-formdata
// need following 2 options
        processData: false,
        contentType: false, // NOT "multipart/form-data"

        // Response is json
        dataType : "json",
        success: handleTransformResponseOk,
        error: handleTransformResponseFail
  });
}

function actionTransformFromInputText() {
  var inputFormat = $("#input-format").val();
  var outputFormat = $("#output-format").val();
  var inputContent = $("#input-as-text").val();
  var url = "rest/jackform-input-text?inputFormat="+inputFormat+"&outputFormat="+outputFormat;

  // clean output first:
  $("#output-content").val("");
  // $.getJSON is a fail, alas, wrt Content-Type missing etc so
  var result = $.ajax({
  	url : url,
  	type: "POST",
     cache: false,
  	// Sending json/xml/csv whatever; not to be processed, as payload;
  	contentType: "application/octet-stream",
  	data: inputContent,
     processData: false,
     // Response is json, however
  	dataType : "json",
  	success: handleTransformResponseOk,
  	error: handleTransformResponseFail
  });
}

function handleTransformResponseOk(resp) {
  // OK only means that call succeeded; not necessarily that the
  // transformation did...
  if (resp.ok) {
    $("#output-content").val(resp.transformed);
    showRow("output-content");
  } else {
    displayTransformFail(resp.errorType, resp.errorMessage);
    hideRow("output-content");
  }  
}

function handleTransformResponseFail(hdr, textStatus, errorString) {
  // First: if no response header or status, must be send failure
  if (!hdr || !hdr.status) {
    var readyState = hdr ? hdr.readyState : -1;
    displayTransformFail("SEND_FAILURE",
"Failed to send the request (no response header) -- server down? (readyState: "+readyState+")");
  } else {
    // Should we try to include "hdr.status" (or 'textStatus')?
    displayTransformFail("INVALID_REQUEST", errorString);
  }
}

function displayTransformFail(failType, failMessage)
{
  if (!failType) failType = "UNKNOWN";
  if (!failMessage) failMessage = "N/A"

  hideRow("output-content");

  $("#output-error-type").text(failType);
  $("#output-error-msg").text(failMessage);

  $("#output-error").dialog("open");
}
