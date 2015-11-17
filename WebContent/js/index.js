//Load the Visualization API and the piechart package.
google.load('visualization', '1.0', {'packages':['corechart']});

$(document).ready(function() {

	$("#metadata-submit").on("click", function(e) {
		e.preventDefault();
		var title = $("#title").val();
		var keywords = $("#keywords").val();
		var summary = $("#summary").val();
		if (validateForm() == true) {
			// request message on server
			xhrPost("api/analysis", title, keywords, summary, function(responseText){
				var keywordArray = $.parseJSON(responseText);
				$("#myTabs li").removeClass('disabled');
				var counter = 1;
				$.each(keywordArray, function(key, item) {
					if (item.relevance < 0.6) {
						return false;
					}
					$("#trend-analysis").append(collapsePanel(item.text, item.relevance, counter));
					drawChart(counter);
					counter += 1;
				});
				$("#trend-tab").tab('show');
			}, function(err){
				console.log(err);
			});
		}
	});

	$("#keywords").tokenfield({delimiter: ","});

});

function validateForm() {
	var summary = $("#summary").val();
	if(summary == "") {
		alert("The abstract is a required field.")
		return false;
	}
	return true;
}

$("#upload").click(function() {
	if (!window.FileReader) {
		alert("Your browser is not supported");
	}
	$("#files").click();
});

$("#files").change(function(e) {
	var input = $("#files").get(0);
	var reader = new FileReader();
	if (input.files.length) {
		var textFile = input.files[0];
		reader.readAsText(textFile);
		$(reader).on("load", processFile);
	}
});

//utilities
function processFile(e) {
	var file = e.target.result;
	$("#summary").val(file);
}

function createXHR(){
	if(typeof XMLHttpRequest != "undefined"){
		return new XMLHttpRequest();
	}else{
		try{
			return new ActiveXObject("Msxml2.XMLHTTP");
		}catch(e){
			try{
				return new ActiveXObject("Microsoft.XMLHTTP");
			}catch(e){}
		}
	}
	return null;
}

function xhrPost(url, title, keywords, summary, callback, errback){
	var formData = new FormData();
	formData.append("title", title);
	formData.append("keywords", keywords);
	formData.append("summary", summary);
	var xhr = new createXHR();
	xhr.open("POST", url, true);
	xhr.onreadystatechange = function(){
		if(xhr.readyState == 4){
			if(xhr.status >= 200){
				callback(xhr.responseText);
			}else{
				errback("service not available");
			}
		}
	};
	xhr.timeout = 3000;
	xhr.ontimeout = errback;
	xhr.send(formData);
}

function collapsePanel(word, relevance, counter) {
	var html = "<div class=\"panel panel-default\">\n<div class=\"panel-heading\">\n" +
	"<h4 class=\"panel-title\">\n"+
	"<a data-toggle=\"collapse\" data-parent=\"#trend-analysis\" href=\"#collapse" + counter + "\">\n" +
	"<b>" + word + "</b>  |  Relevance score:" + relevance +"</a>\n</h4>\n</div>\n" +
	"<div id=\"collapse" + counter + "\" class=\"panel-collapse collapse\">" +
	"<div class=\"panel-body\">Lorem ipsum dolor sit amet, consectetur adipisicing elit," +
	"sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam," +
	"quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat." +
	"</div>\n</div>\n</div>";

	return html;
}


// Callback that creates and populates a data table,
// instantiates the pie chart, passes in the data and
// draws it.
function drawChart(counter) {

	// Create the data table.
	var data = new google.visualization.DataTable();
	data.addColumn('string', 'Topping');
	data.addColumn('number', 'Slices');
	data.addRows([
	              ['Mushrooms', 3],
	              ['Onions', 1],
	              ['Olives', 1],
	              ['Zucchini', 1],
	              ['Pepperoni', 2]
	              ]);

	// Set chart options
	var options = {'title':'How Much Pizza I Ate Last Night',
			'width':400,
			'height':300};

	var id = "collapse" + counter;
	// Instantiate and draw our chart, passing in some options.
	var chart = new google.visualization.PieChart(document.getElementById(id));
	chart.draw(data, options);
}