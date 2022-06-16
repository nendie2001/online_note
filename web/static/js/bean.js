$(function () {
    const app = new Vue({
        el: '#id-bean-main',
        data: {
            beanList: [],
            // 零时数据
            tempBeanList: [],
            activeNames: [],
            loading: true,
            isEmpty: false,
            inputBean: "",
            // 每页多少个
            pageSize: 0,
            // 当前页
            currentPage: 1,
            // 总共多少 bean
            totalBean: 0,
            // 设置最大页码按钮数。
            pagerCount: 5,
            //
            drawer: false,
            direction: 'rtl',
            addBeanId: -1,
            addBeanInput: "",
        },
        created: function () {
            log("created")
            let query = this.parseUrl()
            let index = parseInt(query.page || 1)
            let content = decodeURI(query.content || "")
            log("index, content", index, content)

            let self = this
            if (content.length > 0) {
                self.inputBean = content
                self.loadBean(function () {
                    self.loadBeanWithPageAndInput(index, content)
                    //
                    self.loading = false
                    self.isEmpty = false
                })
            } else {
                window.location.hash = "#?" + `page=${index}`
                this.loadBeanFromIndex(index)
            }
        },
        methods: {
            parseUrl: function () {
                let urlString = window.location.href
                log("url", urlString)
                let urlObj = new URL(urlString);
                let index = urlObj.href.lastIndexOf("?")
                log("index", index)
                log("sub", urlObj.href.substring(index + 1))
                let parameters = urlObj.href.substring(index + 1).split("&")
                log("para", parameters)
                let kv = {}
                for (let i = 0; i < parameters.length; i++) {
                    let p = parameters[i].split("=")
                    let k = p[0]
                    let v = p[1]
                    kv[k] = v
                    log("k v", k, v)
                }
                log("kv", kv)
                return kv
            },
            newBean: function () {
                this.drawer = true
                this.addBeanId = -1
                this.addBeanInput = ""
            },
            handleCurrentChange: function (currentPage) {
                log("cur", currentPage)
                let input = this.inputBean.trim()
                if ((input || "").length > 0) {
                    window.location.hash = "#?" + `page=${currentPage}&content=${input}`
                    this.loadBeanWithPageAndInput(currentPage, input)
                } else {
                    window.location.hash = "#?" + `page=${currentPage}`
                    let number = (currentPage - 1) * this.pageSize
                    this.beanList = this.backUpbeanList.slice(number, number + this.pageSize)
                }
            },
            handleClose(done) {
                let self = this
                notice("warning", "确认要关闭", function () {
                    done();
                    //
                    let input = self.inputBean.trim()
                    log("close", "cur page: ", self.currentPage, input)
                    if ((input || "").length > 0) {
                        self.loadBean(function () {
                            self.loadBeanWithPageAndInput(self.currentPage, input)
                        })
                    } else {
                        self.loadBeanFromIndex(self.currentPage)
                    }
                    // 抽屉输入框重置
                    self.addBeanId = -1
                    self.addBeanInput = ""
                })
            },
            loadBean: function (callback) {
                let url = '/api/bean/all'
                axios.get(url).then(res => {
                    log("res", res)
                    let beans = res.data.beans
                    this.backUpbeanList = beans
                    callback && callback()
                }).catch(err => {
                    log("err", err)
                    notice("error", "网络错误", function () {
                        window.location.href = "/"
                    })
                })
            },
            loadBeanFromIndex: function (pageIndex) {
                let url = '/api/bean/all'
                axios.get(url).then(res => {
                    log("res", res)
                    let beans = res.data.beans
                    this.backUpbeanList = beans
                    if (this.backUpbeanList.length === 0) {
                        this.isEmpty = true
                    } else {
                        this.isEmpty = false
                    }
                    this.totalBean = beans.length
                    this.pageSize = 10
                    if (pageIndex <= 0) {
                        pageIndex = 1
                    }
                    let maxPage = this.totalBean % this.pageSize === 0 ? this.totalBean / this.pageSize : this.totalBean / this.pageSize + 1
                    if (pageIndex > maxPage) {
                        pageIndex = Math.floor(maxPage)
                    }
                    this.currentPage = pageIndex
                    //
                    log("__currentPage", this.currentPage, this.pageSize)
                    let number = (this.currentPage - 1) * this.pageSize
                    log("number", number)
                    this.beanList = beans.slice(number, number + this.pageSize)
                    //
                    this.loading = false
                }).catch(err => {
                    log("err", err)
                    notice("error", "网络错误", function () {
                        window.location.href = "/"
                    })
                })
            },
            findBeanById: function (id) {
                for (let bean of this.beanList) {
                    if (bean.id === id) {
                        return bean
                    }
                }
                return null
            },
            deleteBeanById: function (id, callback) {
                for (let i = 0; i < this.beanList.length; i++) {
                    let bean = this.beanList[i]
                    if (bean.id === id) {
                        this.beanList.splice(i, 1)
                        callback && callback(this)
                        return
                    }
                }
            },
            handleEditClick: function (e, id, content) {
                e.stopPropagation()
                log("handleItemClick", id, content)
                let beanID = parseInt(id)
                let bean = this.findBeanById(beanID)
                this.addBeanId = bean.id
                this.addBeanInput = bean.content
                this.drawer = true
            },
            handleDeleteClick: function (e, id, content) {
                e.stopPropagation()
                swal({
                    title: "确认删除?",
                    icon: "warning",
                    buttons: {
                        yes: "确定",
                        cancel: "取消",
                    }
                }).then((value) => {
                    switch (value) {
                        case "yes":
                            let data = {
                                id: id,
                                deleted: true,
                            }
                            let item = e.target.closest(".el-collapse-item")
                            let self = this
                            $(item).fadeOut("slow", function () {
                                $(item).remove()
                                // 删除显示数据
                                self.deleteBeanById(id, function (self) {
                                    log("this.beanList.length", self.beanList)
                                    if (self.beanList.length === 0) {
                                        self.isEmpty = true
                                    } else {
                                        self.isEmpty = false
                                    }
                                    // 删除 backup 数据
                                    for (let i = 0; i < self.backUpbeanList.length; i++) {
                                        let bean = self.backUpbeanList[i]
                                        if (bean.id === id) {
                                            self.backUpbeanList.splice(i, 1)
                                            return
                                        }
                                    }
                                })
                            });
                            this.update(data)
                        default:
                            break
                    }
                })
            },
            handleBeanDrawerChange: function (id, content) {
                log("drawer change", id, content)
                let data = {
                    id: id,
                    content: content,
                }
                this.update(data)
            },
            handleBlur: function (event, id, content) {
                log("blur", event, id, content)
                if (content.length === 0) {
                    let data = {
                        id: id,
                        deleted: true,
                    }
                    let item = event.target.closest(".el-collapse-item")
                    $(item).fadeOut("slow", function () {
                        $(item).remove();

                    });
                    this.update(data)
                }
            },
            addBean: function () {
                let data = {
                    content: this.addBeanInput,
                }
                log("addBean", data)
                axios.post("/api/bean/add", data).then(res => {
                    log("res", res)
                    let data = res.data
                    let beanId = data.bean.id
                    // 添加需要返回 beanId
                    log("beanId", beanId)
                    this.addBeanId = beanId
                }).catch(err => {
                    log("err", err)
                    notice("error", "网络错误", function () {
                        window.location.href = "/"
                    })
                })
            },
            update: function (data) {
                log('update data', data)
                axios.post("/api/bean/update", data).then(res => {
                    log("res", res)
                }).catch(err => {
                    log("err", err)
                    notice("error", "网络错误", function () {
                        window.location.href = "/"
                    })
                })
            },
            loadBeanWithPageAndInput: function (pageIndex, input) {
                let loadedBeans = []
                for (let bean of this.backUpbeanList) {
                    let content = bean.content.toLowerCase()
                    if (content.includes(input)) {
                        loadedBeans.push(bean)
                    }
                }
                let beans = loadedBeans
                if (beans.length === 0) {
                    this.isEmpty = true
                } else {
                    this.isEmpty = false
                }
                this.totalBean = beans.length
                this.pageSize = 10
                this.currentPage = pageIndex
                //
                let number = (this.currentPage - 1) * this.pageSize
                this.beanList = beans.slice(number, number + this.pageSize)
                //
                if (input.length > 0) {
                    window.location.hash = "#?" + `page=${pageIndex}&content=${input}`
                } else {
                    window.location.hash = "#?" + `page=${pageIndex}`
                }
            },
            handleDrawInput: function () {
                log("change", this.addBeanId)

                if ((this.addBeanInput || "").trim().length === 0) {
                    return
                }

                if (this.addBeanId === -1) {
                    this.addBean()
                } else {
                    let data = {
                        id: this.addBeanId,
                        content: this.addBeanInput,
                    }
                    this.update(data)
                }
            },
            handleSearchInput: function () {
                log("search", this.inputBean)
                let input = this.inputBean.toLowerCase()
                log("input s", input, input.trim().length)
                this.loadBeanWithPageAndInput(this.currentPage, input)
            },
            timeFormator: function (time) {
                return timeForm(time)
            }
        },
    })
})