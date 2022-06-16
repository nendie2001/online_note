$(function () {

    // Vue.component('input-error-notice', {
    //     props: [
    //         'valids'
    //     ],
    //     template: '<div class="input-block"><div v-for="valid in valids"><span class="input-error-notice" v-if="!valid.condition">{{valid.message}}</span></div></div>'
    // })

    const app = new Vue({
        el: 'el-container',
        data: {
            checks: {
                "lengthCheck": function (o) {
                    log("code len", o, o.length)
                    if (o.length === 0) {
                        return false
                    }
                    if (o.length > 0) {
                        return true
                    }
                },
            },
            usernameValids: [
                {
                    name: "lengthCheck",
                    message: "用户名不可为空",
                    condition: true,
                },
            ],
            passwordValids: [
                {
                    name: "lengthCheck",
                    message: "密码不可为空",
                    condition: true,
                },
            ],
            codeValids: [
                {
                    name: "lengthCheck",
                    message: "验证码不可为空",
                    condition: true,
                },
            ],
            username: "",
            password: "",
            code: "",
            refreshUrl: "/verify_code",
        },
        computed: {
            allValid: function () {
                let cond1 = true
                this.usernameValids.forEach((valid) => {
                    let c = valid.condition
                    cond1 = c && cond1
                })
                log("cond1", cond1)
                let cond2 = this.username.length !== 0 && this.password.length !== 0
                    && this.code.length !== 0
                log("cond1", cond2)
                return cond1 && cond2
            },
        },
        watch: {
            username: function() {
                this.checkInput(this.usernameValids, this.username)
            },
            password: function () {
                this.checkInput(this.passwordValids, this.password)
            },
            code: function () {
                this.checkInput(this.codeValids, this.code)
            }
        },
        components: {
            "input-error-notice": input_error_notice,
        },
        methods: {
            checkInput: function(valids, target) {
                for (let valid of valids) {
                    let f = this.checks[valid.name]
                    valid.condition = f(target)
                }
            },
            toIndex: function () {
                log("to Index")
                window.location.href = "/"
            },
            toLogin: function () {
                window.location.href = "/user/login"
            },
            refreshCode: function () {
                log("refreshCode")
                let r = Math.random()
                this.refreshUrl = "/verify_code?" + r
            },
            loginSubmit: function () {
                log("loginSubmit")
                if (!app.allValid) {
                    log("条件不满足")
                    this.checkInput(this.usernameValids, this.username)
                    this.checkInput(this.passwordValids, this.password)
                    this.checkInput(this.codeValids, this.code)
                    return
                }
                let data = {
                    "username": this.username,
                    "password": this.password,
                    "verify_code": this.code,
                }
                axios.post("/api/login", data).then(res => {
                    log("res", res)
                    let data = res.data
                    if (data.msg === "success") {
                        notice("success", "登录成功", function () {
                            window.location = "/bean"
                        })
                    } else {
                        if (data.cause === "verify_code") {
                            notice("error", "验证码错误")
                            this.code = ""
                        } else {
                            notice("error", "用户名或密码错误")
                            this.code = ""
                        }
                    }
                    this.refreshCode()
                }).catch(err => {
                    log("err", err)
                    notice("error", "网络错误")
                })
            },
            register: function () {
                window.location = "/user/register"
            }
        },
    })

})

