$(function () {
    const app = new Vue({
        el: 'el-container',
        data: {
            checks: {
                "signCheck": function (o) {
                    let reg = /^[0-9a-zA-Z_]{1,}$/;
                    let rule = new RegExp(reg)
                    return rule.test(o)
                },
                "lengthCheck": function (o) {
                    return o.length <= 12 && o.length >= 6
                },
                "codeLengthCheck": function (o) {
                    log("code len", o, o.length)
                    if (o.length === 0) {
                        return false
                    }
                    if (o.length > 0) {
                        return true
                    }
                },
            },
            username: "",
            usernameValids: [
                {
                    name: "signCheck",
                    message: "用户名只能包含英文, 数字, 下划线",
                    condition: true,
                },
                {
                    name: "lengthCheck",
                    message: "用户名长度最少为 6, 最长为 20",
                    condition: true,
                },
            ],
            password: "",
            passwordValids: [
                {
                    name: "signCheck",
                    message: "密码只能包含英文, 数字, 下划线",
                    condition: true,
                },
                {
                    name: "lengthCheck",
                    message: "密码长度最少为 6, 最长为 20",
                    condition: true,
                },
            ],
            password2: "",
            passwordIsSame: true,
            code: "",
            codeValids: [
                {
                    name: "codeLengthCheck",
                    message: "验证码不可为空",
                    condition: true,
                },
            ],
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
                let cond2 = this.username.length !== 0 && this.password.length !== 0 &&
                    this.password2.length !== 0 && this.code.length !== 0
                log("cond1", cond2)
                return cond1 && cond2
            },
        },
        watch: {
            username: function() {
                this.checkInput(this.usernameValids, this.username)
            },
            password: function (newString, oldString) {
                this.checkInput(this.passwordValids, this.password)
            },
            password2: function (newString, oldString) {
                this.passwordIsSame = this.password2 === this.password
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
            refreshCode: function () {
                log("refreshCode")
                let r = Math.random()
                this.refreshUrl = "/verify_code?" + r
            },
            toIndex: function () {
                log("to Index")
                window.location.href = "/"
            },
            toLogin: function () {
                window.location.href = "/user/login"
            },
            registerSubmit: function () {
                log("registerSubmit", app.allValid)
                if (!app.allValid) {
                    log("条件不满足")
                    this.checkInput(this.usernameValids, this.username)
                    this.checkInput(this.passwordValids, this.password)
                    this.passwordIsSame = this.password2 === this.password
                    this.checkInput(this.codeValids, this.code)
                    return
                }
                let data = {
                    "username": this.username,
                    "password": this.password,
                    "verify_code": this.code,
                }
                log("提交数据")
                axios.post("/api/register", data).then(res => {
                    log("res", res)
                    let data = res.data
                    if (data.msg === "success") {
                        notice("success", " 注册成功", function () {
                            window.location = "/"
                        })
                    } else {
                        if (data.cause === "verify_code") {
                            notice("error", "验证码错误")
                            this.code = ""
                        }
                    }
                    this.refreshCode()
                }).catch(err => {
                    log("err", err)
                    notice("error", "网络错误")
                })
            },
        },
    })

})

