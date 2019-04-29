/**
 * Created by guenterhesse on 06/09/16.
 */
$("input[name='ip_no']").TouchSpin({
    initval: 1
});

$("input[name='ip_minv']").TouchSpin({
    min: 0,
    max: 100,
    step: 0.1,
    decimals: 2,
    boostat: 5,
    maxboostedstep: 10,
    postfix: '[unit]'
});

$("input[name='ip_maxv']").TouchSpin({
    min: 0,
    max: 100,
    step: 0.1,
    decimals: 2,
    boostat: 5,
    maxboostedstep: 10,
    postfix: '[unit]'
});

$("input[name='ip_frq']").TouchSpin({
    min: 0,
    max: 1000,
    step: 10,
    decimals: 0,
    boostat: 5,
    maxboostedstep: 10,
    postfix: 'ms'
});
