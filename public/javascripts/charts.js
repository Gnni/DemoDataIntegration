Highcharts.chart('erp_data', {

    chart: {
        type: 'line',
        animation: Highcharts.svg,
        height: "350",
        renderTo: 'container'
    },
    title: {
        text: 'ERP Data'
    },
    xAxis: {
        type: 'datetime'//,
        //minRange: 10 * 1000
    },
    yAxis: {
        title: {
            text: 'Number of Business Transactions Last Second',
            tickInterval: 1
        },
        min: 0,
        plotLines: [{
            //value: 0,
            //width: 40,
            color: 'black'
        }]
    },
    plotOptions: {
        series: {
            label: {
                connectorAllowed: false
            },
            pointStart: new Date().getTime()-4000
        }
    },
    series: [{
        name: 'Number of Business Transactions per Second',
        data: [
            [new Date().getTime()-4000,5],
            [new Date().getTime()-3000,0],
            [new Date().getTime()-2000,1],
            [new Date().getTime()-1000,0]
        ],
        color: "black"
    }]

});

Highcharts.chart('iot_data', {

    chart: {
        type: 'line',
        animation: Highcharts.svg,
        height: "350",
    },
    title: {
        text: 'IoT Data'
    },
    xAxis: {
        type: 'datetime'//,
        //minRange: 10 * 1000
    },
    yAxis: {
        title: {
            text: 'Number of Sensor Values Last Second',
            tickInterval: 1
        },
        plotLines: [{
            //value: 0,
            //width: 40,
            color: 'black'
        }]
    },
    plotOptions: {
        series: {
            label: {
                connectorAllowed: false
            },
            pointStart: new Date().getTime()-4000
        }
    },
    series: [{
        name: 'Number of Sensor Values per Second',
        data: [
            [new Date().getTime()-4000,17005],
            [new Date().getTime()-3000,16856],
            [new Date().getTime()-2000,16953],
            [new Date().getTime()-1000,17010]
        ],
        color: "black"
    }]

});

var sqlResChart1 = Highcharts.chart('div_chart_sql_res1', {

    chart: {
        type: 'line',
        animation: Highcharts.svg,
        height: "350",
        width: "1500"
    },
    title: {
        text: ''
    },
    yAxis: [{
        title: {
            text: 'Average Temperature in °C',
            tickInterval: 1,

        },
        labels: {
            format: '{value}°C'
        },
        plotLines: [{
            //value: 0,
            //width: 40,
            color: 'white'
        }]
    },
        {
            title: {
                text: 'Average Noise in dB',
                tickInterval: 1
            },
            labels: {
                format: '{value}dB'
            },
            plotLines: [{
                //value: 0,
                //width: 40,
                color: 'blue'
            }],
            opposite: true
        }
    ],
    plotOptions: {
        series: {
            label: {
                connectorAllowed: false
            }
        }
    }

});

var sqlResChart2 = Highcharts.chart('div_chart_sql_res2', {

    chart: {
        type: 'column',
        animation: Highcharts.svg,
        height: "350",
        width: "1500"
    },
    title: {
        text: ''
    },
    yAxis: [{
        title: {
            text: 'Average Temperature in °C',
            tickInterval: 1
        },
        tickPixelInterval: 25,
        labels: {
            format: '{value}°C'
        },
        plotLines: [{
            //value: 0,
            //width: 40,
            color: 'white'
        }]
    },
        {
            title: {
                text: 'Average Noise in dB',
                tickInterval: 1
            },
            tickPixelInterval: 25,
            labels: {
                format: '{value}dB'
            },
            plotLines: [{
                //value: 0,
                //width: 40,
                color: 'blue'
            }],
            opposite: true
        },
        {
            title: {
                text: 'Average Vibration in mm',
                tickInterval: 1
            },
            tickPixelInterval: 25,
            labels: {
                format: '{value}mm'
            },
            plotLines: [{
                //value: 0,
                //width: 40,
                color: 'green'
            }],
            opposite: true
        }
    ],
    plotOptions: {
        series: {
            label: {
                connectorAllowed: false
            }
        }
    }

});