$(document).ready(function() {

	$('#metadata-submit').on('click', function(e) {
		e.preventDefault();
		var title = $("#title").val(),
			keywords = $("#keywords").val(),
			summary = $("#summary").val();
		if (validateForm() == true) {
			// request message on server
			xhrGet("api/hello", function(responseText){
				alert(responseText);

			}, function(err){
				console.log(err);
			});
		}
	});
	
	$('#keywords').tokenfield({delimiter: ','});

});

function validateForm() {
	var summary = $("#summary").val();
	if(summary == "") {
		alert("The abstract is a required field.")
		return false;
	}
	return true;
}

$('#upload').click(function() {
	if (!window.FileReader) {
		alert('Your browser is not supported');
	}
	$('#files').click();
});

$("#files").change(function(e) {
	var input = $("#files").get(0);
	var reader = new FileReader();
	if (input.files.length) {
		var textFile = input.files[0];
		reader.readAsText(textFile);
		$(reader).on('load', processFile);
	}
});

//utilities
function processFile(e) {
	var file = e.target.result;
	$('#summary').val(file);
}

function createXHR(){
	if(typeof XMLHttpRequest != 'undefined'){
		return new XMLHttpRequest();
	}else{
		try{
			return new ActiveXObject('Msxml2.XMLHTTP');
		}catch(e){
			try{
				return new ActiveXObject('Microsoft.XMLHTTP');
			}catch(e){}
		}
	}
	return null;
}

function xhrGet(url, callback, errback){
	var xhr = new createXHR();
	xhr.open("GET", url, true);
	xhr.onreadystatechange = function(){
		if(xhr.readyState == 4){
			if(xhr.status >= 200){
				callback(xhr.responseText);
			}else{
				errback('service not available');
			}
		}
	};
	xhr.timeout = 3000;
	xhr.ontimeout = errback;
	xhr.send();
}
