/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 28.733348532716672, "KoPercent": 71.26665146728332};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.28733348532716674, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.42969132778049973, 500, 1500, "GET - KeyValue Read (system_db)"], "isController": false}, {"data": [0.0, 500, 1500, "POST - Document Insert (Write)"], "isController": false}, {"data": [1.0, 500, 1500, "GET - Document Point Read"], "isController": false}, {"data": [0.0, 500, 1500, "POST - KeyValue Write (system_db)"], "isController": false}, {"data": [1.0, 500, 1500, "POST - Auth Login"], "isController": false}, {"data": [0.0, 500, 1500, "POST - TimeSeries Metric Telemetry Ingestion"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 30703, 21881, 71.26665146728332, 0.38641175129466304, 0, 18, 0.0, 1.0, 1.0, 2.0, 512.6565369844716, 445.91950476916014, 191.14643121764902], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET - KeyValue Read (system_db)", 6123, 3492, 57.030867221950025, 0.20202515106973737, 0, 4, 0.0, 1.0, 1.0, 1.0, 102.98371905274489, 83.86248536207447, 26.32208455622645], "isController": false}, {"data": ["POST - Document Insert (Write)", 6148, 6148, 100.0, 0.5196811971372791, 0, 11, 0.0, 1.0, 1.0, 2.0, 102.74750986028478, 94.9210393826459, 58.286595339762016], "isController": false}, {"data": ["GET - Document Point Read", 6141, 0, 0.0, 0.23286109754111706, 0, 7, 0.0, 1.0, 1.0, 2.0, 102.77308252305325, 107.2788872786722, 28.20237909079879], "isController": false}, {"data": ["POST - KeyValue Write (system_db)", 6131, 6131, 100.0, 0.4871962159517195, 0, 8, 0.0, 1.0, 1.0, 2.0, 102.8484197812521, 80.55120377398846, 35.390430382095886], "isController": false}, {"data": ["POST - Auth Login", 50, 0, 0.0, 1.24, 0, 18, 1.0, 2.0, 2.0, 18.0, 5.122425980944575, 4.3920800891302125, 1.3256278173342897], "isController": false}, {"data": ["POST - TimeSeries Metric Telemetry Ingestion", 6110, 6110, 100.0, 0.4833060556464807, 0, 7, 0.0, 1.0, 1.0, 2.0, 102.88966725043782, 80.58350892075306, 43.56908663318402], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": [{"data": ["Test failed: code expected to equal /\\n\\n****** received  : 20[[[1]]]\\n\\n****** comparison: 20[[[0]]]\\n\\n/", 18389, 84.04094876833783, 59.89317004852946], "isController": false}, {"data": ["404/Not Found", 3492, 15.959051231662173, 11.373481418753867], "isController": false}]}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 30703, 21881, "Test failed: code expected to equal /\\n\\n****** received  : 20[[[1]]]\\n\\n****** comparison: 20[[[0]]]\\n\\n/", 18389, "404/Not Found", 3492, "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": ["GET - KeyValue Read (system_db)", 6123, 3492, "404/Not Found", 3492, "", "", "", "", "", "", "", ""], "isController": false}, {"data": ["POST - Document Insert (Write)", 6148, 6148, "Test failed: code expected to equal /\\n\\n****** received  : 20[[[1]]]\\n\\n****** comparison: 20[[[0]]]\\n\\n/", 6148, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["POST - KeyValue Write (system_db)", 6131, 6131, "Test failed: code expected to equal /\\n\\n****** received  : 20[[[1]]]\\n\\n****** comparison: 20[[[0]]]\\n\\n/", 6131, "", "", "", "", "", "", "", ""], "isController": false}, {"data": [], "isController": false}, {"data": ["POST - TimeSeries Metric Telemetry Ingestion", 6110, 6110, "Test failed: code expected to equal /\\n\\n****** received  : 20[[[1]]]\\n\\n****** comparison: 20[[[0]]]\\n\\n/", 6110, "", "", "", "", "", "", "", ""], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
