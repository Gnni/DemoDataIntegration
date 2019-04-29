var numberOfSensors = 9;
var isStarted = "Stop Simulation";
var isStopped = "Start Simulation";

$("#submitStartDatagen").click(function (event) {

    var button = document.getElementById("submitStartDatagen");
    var stopRoute = "/stopdatageneration";
    var startRoute = "/startdatageneration";
    var route = stopRoute;
    var newText = isStopped;

    if (button.value === isStopped) {
        route = startRoute;
        newText = isStarted;
        $.ajax({
            url: route
        })
            .done(function () {
                button.value = newText;
            });
        setTimeout(function () {
            connectToWs();
        }, 1000);
        setTimeout(function () {
            activateDiagramUpdates();
        }, 1500);

    } else {
        button.value = newText;
        numberOfSensors = 9;
        pauseDiagramUpdates();
        console.log("Diagram paused!");
        closeConn();
        $.ajax({
            url: route
        })
            .done(function () {
                button.value = newText;
                numberOfSensors = 9;
            });
    }

    event.preventDefault();
});

$("#submitAddSensors").click(function (event) {
    if( document.getElementById("submitStartDatagen").value.localeCompare(isStarted) != 0 ) {
        alert("Please Start Simulation Before Adding Sensor(s).");
    } else {
        var numberOfNewSensors = $('#ip_no').val();
        if (numberOfNewSensors > 0) {
            var workplace = $('#ip_wp').val();
            var sensorType = $('#ip_st').val();
            var minValue = $('#ip_minv').val();
            var maxValue = $('#ip_maxv').val();
            var freq = $('#ip_frq').val();

            console.log("Adding sensor(s): number: " + numberOfNewSensors + " wp: " + workplace + " st: " + sensorType + " minval: " + minValue + " maxVal: " + maxValue + " frequency: " + freq);
            $.ajax({
                url: "/addsensors/" + numberOfNewSensors + "/" + workplace + "/" + sensorType + "/" + minValue + "/" + maxValue + "/" + freq
            })
                .done(function () {
                    console.log("Sensors added!");
                    addRowsToTable(numberOfNewSensors, workplace, sensorType, minValue, maxValue, freq);
                });
        }
    }
    event.preventDefault();
});

function addRowsToTable(numberOfNewSensors, workplace, sensorType, minValue, maxValue, freq) {
    var html = '';
    for (i = 1; i <= numberOfNewSensors; i++) {
        html += ('<tr><td>' + workplace + '</td><td>' + sensorType + '</td><td>' + minValue + ' - ' + maxValue + '[unit]</td><td>' + freq + ' ms</td></tr>');
    }
    var newRow = jQuery(html);
    jQuery('table.table').append(newRow);
}