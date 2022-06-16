// Vue.component('input-error-notice', {
//     props: [
//         'valids'
//     ],
//     template: '<div class="input-block"><div v-for="valid in valids"><span class="input-error-notice" v-if="!valid.condition">{{valid.message}}</span></div></div>'
// })
const input_error_notice = {
    name: "input-error-notice",
    props: [
        'valids'
    ],
    template: '<div class="input-block"><div v-for="valid in valids"><span class="input-error-notice" v-if="!valid.condition">{{valid.message}}</span></div></div>'
}