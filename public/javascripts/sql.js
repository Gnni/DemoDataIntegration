$("#submitSql").click(function (event) {
    executeSql();
    event.preventDefault();
});

function executeSql() {
    var button = document.getElementById("submitSql");
    var oldButtonValue = button.value;
    button.value = "Loading...";
    var sql = document.getElementById("sqlStatement");
    var route = "/executesql"

    $.ajax({
        url: route,
        type: "POST",
        data: {sql: sql.value},
        success: function (data) {
            var jsonObj = JSON.parse(data);
            document.getElementById('sqlResultTableArea').innerHTML = json2table(jsonObj, 'table table-striped');
            $('#sqlResultTable').DataTable( {
                "paging":   false,
                "info":     false
            });
            addDataToChart(jsonObj)
        }
    })
        .done(function () {
            button.value = oldButtonValue;
        });
    setTimeout(function () {
    }, 1000);
    setTimeout(function () {
    }, 1500);
}

function addDataToChart(jsonObj) {

    if (jsonObj != null &&
        jsonObj[0].hasOwnProperty('average_temp_in_c') &&
        jsonObj[0].hasOwnProperty('average_noise_in_db') &&
        jsonObj[0].hasOwnProperty('production_order_id')
    ) {

        var productionOrderIdArray = [];
        var avgTempArray = [];
        var avgNoiseArray = [];
        for (var i = 0; i < jsonObj.length; ++i) {
            productionOrderIdArray.push(jsonObj[i].production_order_id);
            avgTempArray.push(jsonObj[i].average_temp_in_c);
            avgNoiseArray.push(jsonObj[i].average_noise_in_db);
        }
        document.getElementById("container_sql_res1_chart").style = "display: flex;";
        sqlResChart1.xAxis[0].categories = productionOrderIdArray;
        while(sqlResChart1.series.length > 0)
            sqlResChart1.series[0].remove(true);
        sqlResChart1.addSeries({
            name: "Average Temperature",
            data: avgTempArray,
            color: "pink",
            lineWidth: 5,
            tooltip: {
                valueSuffix: '°C'
            },
            tooltip: {
                pointFormatter: function () { return "Average Temperature: " + this.y.toFixed(2) + "°C"; }
            }
        });
        sqlResChart1.addSeries({
            name: "Average Noise",
            data: avgNoiseArray,
            yAxis: 1,
            color: "darkblue",
            lineWidth: 5,
            tooltip: {
                valueSuffix: 'dB'
            },
            tooltip: {
                pointFormatter: function () { return "Average Noise: " + this.y.toFixed(2) + "dB"; }
            }
        });
        document.getElementById("container_sql_res2_chart").style = "display: none;";

    } else if(jsonObj != null &&
        jsonObj[0].hasOwnProperty('average_temp_in_c') &&
        jsonObj[0].hasOwnProperty('average_noise_in_db') &&
        jsonObj[0].hasOwnProperty('average_vibration_in_mm')) {

        var supplierIdArray = [];
        var avgTempArray = [];
        var avgNoiseArray = [];
        var avgVibrationArray = [];
        for (var i = 0; i < jsonObj.length; ++i) {
            supplierIdArray.push("Supplier #" + jsonObj[i].supplier);
            avgTempArray.push(jsonObj[i].average_temp_in_c);
            avgNoiseArray.push(jsonObj[i].average_noise_in_db);
            avgVibrationArray.push(jsonObj[i].average_vibration_in_mm);
        }
        document.getElementById("container_sql_res2_chart").style = "display: flex;";
        sqlResChart2.xAxis[0].categories = supplierIdArray;
        while(sqlResChart2.series.length > 0)
            sqlResChart2.series[0].remove(true);
        sqlResChart2.addSeries({
            name: "Average Temperature",
            data: avgTempArray,
            color: "pink",
            lineWidth: 5,
            tooltip: {
                valueSuffix: '°C'
            },
            tooltip: {
                pointFormatter: function () { return "Average Temperature: " + this.y.toFixed(2) + "°C"; }
            }
        });
        sqlResChart2.addSeries({
            name: "Average Noise",
            data: avgNoiseArray,
            yAxis: 1,
            color: "darkblue",
            lineWidth: 5,
            tooltip: {
                valueSuffix: 'dB'
            },
            tooltip: {
                pointFormatter: function () { return "Average Noise: " + this.y.toFixed(2) + "dB"; }
            }
        });
        sqlResChart2.addSeries({
            name: "Average Vibration",
            data: avgVibrationArray,
            yAxis: 2,
            color: "green",
            lineWidth: 5,
            tooltip: {
                valueSuffix: 'mm'
            },
            tooltip: {
                pointFormatter: function () { return "Average Vibration: " + this.y.toFixed(4) + "mm"; }
            }
        });
        document.getElementById("container_sql_res1_chart").style = "display: none;";

    } else {
        document.getElementById("container_sql_res1_chart").style = "display: none;";
        document.getElementById("container_sql_res2_chart").style = "display: none;";
    }
}

function pad( num ) {
    num = "0" + num;
    return num.slice( -2 );
}

function json2table(json, classes) {
    var cols = Object.keys(json[0]);

    var headerRow = '';
    var bodyRows = '';

    classes = classes || '';

    cols.map(function(col) {
        col = col.replace(/_/g, ' ');
        col = col.replace(/\b\w/g, function(l){ return l.toUpperCase() });
        headerRow += '<th>' + col + '</th>';
    });

    json.map(function(row) {
        bodyRows += '<tr>';

        cols.map(function(colName) {
            if (isNaN(row[colName]) === false && parseInt(row[colName]) > 1000000000000) {
                var year, month, day, hour, minutes, d, finalDate;
                d = new Date(parseInt(row[colName]));
                year = d.getFullYear();
                month = pad( d.getMonth() + 1 );
                day = pad( d.getDate() );
                hour = pad( d.getHours() );
                minutes = pad( d.getMinutes() );

                finalDate =  year + "-" + month + "-" + day + " " + hour + ":" + minutes;
                bodyRows += '<td>' + finalDate + '</td>';
            } else {
                bodyRows += '<td>' + row[colName] + '</td>';
            }
        })

        bodyRows += '</tr>';
    });

    return '<table id="sqlResultTable" class="' +
        classes +
        '"><thead><tr>' +
        headerRow +
        '</tr></thead><tbody>' +
        bodyRows +
        '</tbody></table>';
}

$("#sql1").click(function (event) {
    var sql = "select POP.PRODUCTION_ORDER_ID,\n" +
        "POP.WORKPLACE_ACTUAL as workplace,\n" +
        "MIN(SENSORS_TEMP.DATE) as time_entered,\n" +
        "MAX(SENSORS_TEMP.DATE) as time_left,\n" +
        "AVG(SENSORS_TEMP.TEMPERATURE_VALUE) as average_temp_in_c,\n" +
        "AVG(SENSORS_NOISE.NOISE_VALUE) as average_noise_in_db\n" +
        "\n" +
        "from \"INDUSTRY\".\"PRODUCTION_ORDER_POSITION\" as POP\n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_TEMP\n" +
        "ON POP.START_TS <= SENSORS_TEMP.DATE \n" +
        "AND POP.END_TS >= SENSORS_TEMP.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_TEMP.WORKPLACE_ID \n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_NOISE\n" +
        "ON POP.START_TS <= SENSORS_NOISE.DATE \n" +
        "AND POP.END_TS >= SENSORS_NOISE.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_NOISE.WORKPLACE_ID \n" +
        "\n" +
        "where \n" +
        "SENSORS_TEMP.TEMPERATURE_UNIT = 'C' and \n" +
        "SENSORS_NOISE.NOISE_UNIT = 'dB' and \n" +
        "POP.WORKPLACE_ACTUAL = '2' and \n" +
        "START_TS > '2019-02-10 0:00:01.000'\n" +
        "group by POP.PRODUCTION_ORDER_ID,\n" +
        "POP.WORKPLACE_ACTUAL\n" +
        "order by MAX(SENSORS_TEMP.DATE) asc";

    document.getElementById("sqlStatement").value = "select POP.PRODUCTION_ORDER_ID,\n" +
        "POP.WORKPLACE_ACTUAL as workplace,\n" +
        "MIN(SENSORS_TEMP.DATE) as time_entered,\n" +
        "MAX(SENSORS_TEMP.DATE) as time_left,\n" +
        "AVG(SENSORS_TEMP.TEMPERATURE_VALUE) as average_temp_in_c,\n" +
        "AVG(SENSORS_NOISE.NOISE_VALUE) as average_noise_in_db\n" +
        "\n" +
        "from \"INDUSTRY\".\"PRODUCTION_ORDER_POSITION\" as POP\n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_TEMP\n" +
        "ON POP.START_TS <= SENSORS_TEMP.DATE \n" +
        "AND POP.END_TS >= SENSORS_TEMP.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_TEMP.WORKPLACE_ID \n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_NOISE\n" +
        "ON POP.START_TS <= SENSORS_NOISE.DATE \n" +
        "AND POP.END_TS >= SENSORS_NOISE.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_NOISE.WORKPLACE_ID \n" +
        "\n" +
        "where \n" +
        "SENSORS_TEMP.TEMPERATURE_UNIT = 'C' and \n" +
        "SENSORS_NOISE.NOISE_UNIT = 'dB' and \n" +
        "POP.WORKPLACE_ACTUAL = '2' and \n" +
        "START_TS > '2019-02-10 0:00:01.000'\n" +
        "group by POP.PRODUCTION_ORDER_ID,\n" +
        "POP.WORKPLACE_ACTUAL\n" +
        "order by MAX(SENSORS_TEMP.DATE) asc";
    document.getElementById("sqlStatement").innerHTML = sql;
    document.getElementById("sqlStatement").value = sql;

    executeSql();
    event.preventDefault();
});

$("#sql2").click(function (event) {

    var sql = "select \n" +
        "POP.WORKPLACE_ACTUAL as workplace,\n" +
        "GR.SUPPLIER as supplier,\n" +
        "AVG(SENSORS_TEMP.TEMPERATURE_VALUE) as average_temp_in_c,\n" +
        "AVG(SENSORS_NOISE.NOISE_VALUE) as average_noise_in_db,\n" +
        "AVG(SENSORS_VIBRATION.VIBRATION_VALUE)/100 as average_vibration_in_mm\n" +
        "\n" +
        "from \"INDUSTRY\".\"PRODUCTION_ORDER_POSITION\" as POP\n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_VIBRATION\n" +
        "ON POP.START_TS <= SENSORS_VIBRATION.DATE \n" +
        "AND POP.END_TS >= SENSORS_VIBRATION.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_VIBRATION.WORKPLACE_ID \n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_TEMP\n" +
        "ON POP.START_TS <= SENSORS_TEMP.DATE \n" +
        "AND POP.END_TS >= SENSORS_TEMP.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_TEMP.WORKPLACE_ID \n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_NOISE\n" +
        "ON POP.START_TS <= SENSORS_NOISE.DATE \n" +
        "AND POP.END_TS >= SENSORS_NOISE.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_NOISE.WORKPLACE_ID \n" +
        "\n" +
        "INNER JOIN \"INDUSTRY\".\"GOODS_RECEIVED\" as GR\n" +
        "ON GR.ID = POP.GOODS_RECEIVED_ID \n" +
        "\n" +
        "where \n" +
        "SENSORS_VIBRATION.VIBRATION_UNIT = 'MM' and \n" +
        "SENSORS_NOISE.NOISE_UNIT = 'dB' and \n" +
        "SENSORS_TEMP.TEMPERATURE_UNIT = 'C' and \n" +
        "POP.WORKPLACE_ACTUAL = 4 \n" +
        "AND POP.START_TS > '2017-01-24 15:25:35.000' \n" +
        "AND POP.START_TS < '2017-01-24 15:36:01.000' \n" +
        "group by \n" +
        "GR.SUPPLIER,\n" +
        "POP.WORKPLACE_ACTUAL\n" +
        "order by MAX(SENSORS_VIBRATION.DATE) desc";

    document.getElementById("sqlStatement").value = "select \n" +
        "POP.WORKPLACE_ACTUAL as workplace,\n" +
        "GR.SUPPLIER as supplier,\n" +
        "AVG(SENSORS_TEMP.TEMPERATURE_VALUE) as average_temp_in_c,\n" +
        "AVG(SENSORS_NOISE.NOISE_VALUE) as average_noise_in_db,\n" +
        "AVG(SENSORS_VIBRATION.VIBRATION_VALUE)/100 as average_vibration_in_mm\n" +
        "\n" +
        "from \"INDUSTRY\".\"PRODUCTION_ORDER_POSITION\" as POP\n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_VIBRATION\n" +
        "ON POP.START_TS <= SENSORS_VIBRATION.DATE \n" +
        "AND POP.END_TS >= SENSORS_VIBRATION.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_VIBRATION.WORKPLACE_ID \n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_TEMP\n" +
        "ON POP.START_TS <= SENSORS_TEMP.DATE \n" +
        "AND POP.END_TS >= SENSORS_TEMP.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_TEMP.WORKPLACE_ID \n" +
        "\n" +
        "LEFT OUTER JOIN \"INDUSTRY\".\"MEASURED_DATA\" AS SENSORS_NOISE\n" +
        "ON POP.START_TS <= SENSORS_NOISE.DATE \n" +
        "AND POP.END_TS >= SENSORS_NOISE.DATE \n" +
        "AND POP.WORKPLACE_ACTUAL = SENSORS_NOISE.WORKPLACE_ID \n" +
        "\n" +
        "INNER JOIN \"INDUSTRY\".\"GOODS_RECEIVED\" as GR\n" +
        "ON GR.ID = POP.GOODS_RECEIVED_ID \n" +
        "\n" +
        "where \n" +
        "SENSORS_VIBRATION.VIBRATION_UNIT = 'MM' and \n" +
        "SENSORS_NOISE.NOISE_UNIT = 'dB' and \n" +
        "SENSORS_TEMP.TEMPERATURE_UNIT = 'C' and \n" +
        "POP.WORKPLACE_ACTUAL = 4 \n" +
        "AND POP.START_TS > '2017-01-24 15:25:35.000' \n" +
        "AND POP.START_TS < '2017-01-24 15:36:01.000' \n" +
        "group by \n" +
        "GR.SUPPLIER,\n" +
        "POP.WORKPLACE_ACTUAL\n" +
        "order by MAX(SENSORS_VIBRATION.DATE) desc";
    document.getElementById("sqlStatement").innerHTML = sql;
    document.getElementById("sqlStatement").value = sql;

    executeSql();
    event.preventDefault();
});