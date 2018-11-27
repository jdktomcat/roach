var $;
var layer;
var table;
var step = 0;
var rowDataId;
var data = null;

window.onload = function () {

    function isReady() {
        if (step < 160 && !window.connectRouter) {
            step++;
            setTimeout(isReady, 10);
        } else {
            initPage();
        }
    }

    isReady();
};


/**初始化页面信息*/
function initPage() {
    layui.use(['jquery', 'table', 'layer'], function () {
        $ = layui.jquery;
        layer = layui.layer;
        table = layui.table;
        data = connectRouter.selectConnect();
        initConnectData();
    });
}


/**初始化连接数据*/
function initConnectData() {
    table.render({
        id: 'dataList',
        elem: '#dataList',
        height: 'full-70',
        data: JSON.parse(data),
        cols: [[
            {field: 'text', title: '名称', event: 'setSign'},
            {
                title: '主机', event: 'setSign',
                templet: function (data) {
                    if (data.type === '0') {
                        return data.rhost;
                    } else {
                        return data.shost;
                    }
                }
            },
            {field: 'rport', title: '端口', event: 'setSign'},
            {
                title: '类型', event: 'setSign',
                templet: function (data) {
                    if (data.isha === '0') {
                        return "单机";
                    } else {
                        return "集群";
                    }
                }
            },
            {
                title: 'SSH', event: 'setSign',
                templet: function (data) {
                    if (data.type === '0') {
                        return "关闭";
                    } else {
                        return "启用";
                    }
                }
            },
            {field: 'time', title: '时间', event: 'setSign'},
        ]],
        page: {
            layout: ['prev', 'page', 'next', 'count', 'skip']
        },
        done: function (res) {
            layer.msg('双击行连接服务!');
            var tbody = $('#tableDiv').find('.layui-table-body').find("table").find("tbody");
            //单击行选中数据
            tbody.children("tr").on('click', function () {
                var id = JSON.stringify(tbody.find(".layui-table-hover").data('index'));
                var obj = res.data[id];
                rowDataId = obj.id;
            });
            // //双击行连接服务
            tbody.children("tr").on('dblclick', function () {
                var id = JSON.stringify(tbody.find(".layui-table-hover").data('index'));
                var obj = res.data[id];
                openConnect(obj.id);
            });
            rowDataId = '';
        }
    });
}

/**添加连接数据*/
function addConnectData() {
    layer.open({
        title: '新增连接',
        type: 2,
        area: ['455px', '445px'],
        fixed: true,
        maxmin: false,
        resize: false,
        skin: 'layui-layer-lan',
        content: '../page/connect-save.html'
    });
}

/**刷新连接列表*/
function getConnectData() {
    layer.load(2);
    var data = connectRouter.selectConnect();
    table.reload('dataList', {
        height: 'full-70',
        data: JSON.parse(data),
        page: {curr: 1},
        done: function (res) {
            var tbody = $('#tableDiv').find('.layui-table-body').find("table").find("tbody");
            //单击行选中数据
            tbody.children("tr").on('click', function () {
                var id = JSON.stringify(tbody.find(".layui-table-hover").data('index'));
                var obj = res.data[id];
                rowDataId = obj.id;
            });
            // //双击行连接服务
            tbody.children("tr").on('dblclick', function () {
                var id = JSON.stringify(tbody.find(".layui-table-hover").data('index'));
                var obj = res.data[id];
                openConnect(obj.id);
            });
            rowDataId = '';
            layer.closeAll('loading');
        }
    });
}

/**编辑连接数据*/
function updConnectData() {
    if (rowDataId === "" || rowDataId == null) {
        layer.msg('请选择要操作的数据行！');
        return false;
    }
    layer.open({
        title: '编辑连接',
        type: 2,
        area: ['455px', '445px'],
        fixed: true,
        maxmin: false,
        resize: false,
        skin: 'layui-layer-lan',
        content: '../page/connect-edit.html'
    });
}

/**删除连接数据*/
function delConnectData() {
    if (rowDataId === "" || rowDataId == null) {
        layer.msg('请选择要操作的数据行！');
        return false;
    }
    var index = layer.confirm('确认删除连接？', {
        btn: ['确定', '取消'],
        skin: 'layui-layer-lan',
        closeBtn: 0
    }, function () {
        var json = parent.connectRouter.deleteConnect(rowDataId)
        var data = JSON.parse(json);
        layer.close(index);
        if (data.code === 200) {
            getConnectData();
        } else {
            layer.alert(data.msgs, {
                skin: 'layui-layer-lan',
                closeBtn: 0
            });
        }
    });
}


/**操作连接数据*/
function setConnectData() {
    if (rowDataId === "" || rowDataId == null) {
        layer.msg('请选择要操作的数据行！');
        return false;
    }
    layer.open({
        title: '操作连接',
        type: 2,
        area: ['455px', '480px'],
        fixed: true,
        maxmin: false,
        skin: 'layui-layer-lan',
        content: '../page/connect-info.html'
    });
}

/**打开连接数据*/
function openConnect(id) {
    var result = 0;
    layer.load(2);
    var json = connectRouter.createConnect(id);
    var data = JSON.parse(json);
    if (data.code === 200) {
        result = 1;
    } else {
        layer.alert(data.msgs, {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
    }
    layer.closeAll('loading');
    return result;
}

/**断开连接数据*/
function closeConnect(id) {
    var result = 0;
    layer.load(2);
    var json = connectRouter.disconConnect(id);
    var data = JSON.parse(json);
    if (data.code === 200) {
        result = 1;
    }
    layer.closeAll('loading');
    return result;
}

/**
 * 导出连接
 */
function expConnectData() {
    layer.msg("备份连接任务正在后台执行...");
    var json = connectRouter.backupConnect();
    var data = JSON.parse(json);
    if (data.code === 200) {
        layer.msg(data.msgs);
    } else {
        layer.alert(data.msgs, {
            skin: 'layui-layer-lan',
            closeBtn: 0
        });
    }
}


/**
 * 导入连接
 */
function impConnectData() {
    layer.msg("还原连接任务正在后台执行...");
    var json = connectRouter.recoveConnect();
    var data = JSON.parse(json);
    layer.msg(data.msgs);
    getConnectData();
}








