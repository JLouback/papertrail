//Load the Visualization API and the piechart package.
google.load('visualization', '1.0', {'packages':['corechart']});

// Fill in the analysis data and restore original input data
$(document).ready(function() {
	putFormData();
	gTrend();
	academiaTrend();
	putCitations();
	$("#keywords").tokenfield({delimiter: ","});
});

// Puts back form data
function putFormData() {
	$("#title").val(localStorage.getItem('title'));
	$("#keywords").val(localStorage.getItem('keywords'));
	$("#summary").val(localStorage.getItem('summary'));
}

// Builds and inserts google search trends chart
function gTrend() {
	var analysis = $.parseJSON(localStorage.getItem('alchemy'));
	console.log("From server proc:");
	console.log(analysis);
	var keywordArray = analysis.trends;
	console.log(analysis.trends);
	console.log(keywordArray);
	var query = "";
	var counter = 1;
	$.each(keywordArray, function(key, item) {
		if (counter > 1) {
			query = query + ",";
		}
		query = query + item.text;
		if (counter == Math.min(5, keywordArray.length)) {
			return false;
		}
		counter += 1;
 	});
	var gchart = '<iframe width="620" height="340" src="http://www.google.com/trends/fetchComponent?hl=en-US&q=' +
				 query +
				 '&content=1&cid=TIMESERIES_GRAPH_0&export=5&w=600&h=320" style="border: none;"></iframe>';
	$("#collapse1").append(gchart);
}

// Builds and inserts academia trend charts
function academiaTrend() {
	var analysis = $.parseJSON(localStorage.getItem('alchemy'));
	var keywordArray = $.parseJSON(analysis['trends']);
	var counter = 2;
	$.each(keywordArray, function(key, item) {
		$("#trend-analysis").append(collapsePanel(item.text, item.relevance, counter));
		var trend = item['trend'];
		drawChart(item.text, counter, trend);
		counter += 1;
	});
}

//Returns html of citation recommendation entry
function citation(title, author, year, summary) {
	var html = "<h3>" + title + "</h3>\n" +
			   "<p><b>" + author + ", " + year + "</b></p>\n" + 
			   "<p>" + summary + "</p>\n";
	return html;
}

// Writes list of recommended citations
function putCitations() {
	var analysis = $.parseJSON(localStorage.getItem('alchemy'));
	console.log("From server2:");
	console.log(analysis);
	var citationArray = $.parseJSON(analysis['citations']);
	console.log(citationArray);
	$.each(citationArray, function(key, item) {
		$("#citation-analysis").append(citation(item.title, item.authors, item.year, item.summary));
	});
}

// Structure for the collapsible pannel
function collapsePanel(word, relevance, counter) {
	var html = "<div class=\"panel panel-default\">\n<div class=\"panel-heading\">\n" +
	"<h4 class=\"panel-title\">\n"+
	"<a data-toggle=\"collapse\" data-parent=\"#trend-analysis\" href=\"#collapse" + counter + "\">\n" +
	"<b>Academia Trend: " + word + "</b>  |  Relevance score:" + relevance +"</a>\n</h4>\n</div>\n" +
	"<div id=\"collapse" + counter + "\" class=\"panel-collapse collapse\">" +
	"<div id=\"graph" + counter + "\"></div>" +
	"<div class=\"panel-body disclaimer\" id=\"description" + counter + "\"></div>\n</div>\n</div>";
	return html;
}

// Callback that creates and populates a google bar chart
function drawChart(word, counter, trend) {
	// Create the data table.
	var data = new google.visualization.DataTable();
    data.addColumn('date', 'Year');
    data.addColumn('number', 'Publications');
    // Tooltip with only year
    data.addColumn({type: 'string', role: 'tooltip'});
    var total = 0;
    for (var year in trend) {
    	if (trend.hasOwnProperty(year)) {
    		data.addRow([new Date(year, 1, 1), trend[year], year + ':' + trend[year]]);
    		total += trend[year];
    	}
    }
    var description = "Total of " + total + " publications. Disclaimer: The graph above lists publications in our database, limited to " +
    				  "entries prior to February 2011.";
    $("#description" + counter).append(description);
	// Set chart options
    //Random colors
    var colors = ['red', 'cyan', 'green', 'orange'];
    var color = [colors[Math.floor(Math.random() * colors.length)]];
	var options = {'title':'Number of Publications Containing Keyword','width':600,'height':300, 'colors': color};

	var id = "graph" + counter;
	// Instantiate and draw our chart, passing in some options.
	var chart = new google.visualization.ColumnChart(document.getElementById(id));
	chart.draw(data, options);
}