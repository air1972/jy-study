//带web前缀，本地时为localhost:8080/web
function getFullCtxPath() {
    var pathName = window.location.pathname.substring(1);
    var webName = pathName === '' ? '' : pathName.substring(0, pathName.indexOf('/'));
    return window.location.protocol + '//' + window.location.host + '/' + webName;
}

// 处理需要登录的操作，返回true表示需要登录
function handleLoginRequired(callback) {

    // 检查会话状态
    $.ajax({
        url: ctx + "web/checkLogin",
        type: "GET",
        async: false,
        success: function(res) {
            window.isAuthenticated = res.code === 0;
        },
        error: function() {
            window.isAuthenticated = false;
        }
    });

    // 如果已经登录，直接返回false
    if (window.isAuthenticated === true) {
        return false;
    }

    // 未登录，显示登录确认框
    layer.confirm('此功能需要登录，是否去登录？', {
        btn: ['去登录','取消'],
        title: '提示'
    }, function(){
        window.location.href = ctx + 'login';
    });
    return true;
}

// 处理点赞
function handleLike(type, targetId, onSuccess) {
    // 如果需要登录，直接返回
    if(handleLoginRequired()) {
        return;
    }
    
    $.ajax({
        url: ctx + "web/interaction/like",
        type: "POST", 
        data: {
            type: type,
            targetId: targetId
        },
        success: function(res) {
            if(res.code === 0) {
                $.modal.msgSuccess('点赞成功');
                if(onSuccess) onSuccess(res);
            } else {
                // 如果返回未登录错误，更新登录状态并重新检查
                if(res.msg && res.msg.indexOf('登录') !== -1) {
                    window.isAuthenticated = false;
                    handleLoginRequired();
                } else {
                    $.modal.msgError(res.msg);
                }
            }
        }
    });
}

// 处理取消点赞
function handleUnlike(type, targetId, onSuccess) {
    // 如果需要登录，直接返回
    if(handleLoginRequired()) {
        return;
    }
    
    $.ajax({
        url: ctx + "web/interaction/unlike",
        type: "POST",
        data: {
            type: type,
            targetId: targetId
        },
        success: function(res) {
            if(res.code === 0) {
                $.modal.msgSuccess('已取消点赞');
                if(onSuccess) onSuccess(res);
            } else {
                // 如果返回未登录错误，更新登录状态并重新检查
                if(res.msg && res.msg.indexOf('登录') !== -1) {
                    window.isAuthenticated = false;
                    handleLoginRequired();
                } else {
                    $.modal.msgError(res.msg);
                }
            }
        }
    });
}

// 处理收藏
function handleCollect(type, targetId, onSuccess) {
    // 如果需要登录，直接返回
    if(handleLoginRequired()) {
        return;
    }
    
    $.ajax({
        url: ctx + "web/interaction/collect",
        type: "POST",
        data: {
            type: type,
            targetId: targetId
        },
        success: function(res) {
            if(res.code === 0) {
                $.modal.msgSuccess('收藏成功');
                if(onSuccess) onSuccess(res);
            } else {
                // 如果返回未登录错误，更新登录状态并重新检查
                if(res.msg && res.msg.indexOf('登录') !== -1) {
                    window.isAuthenticated = false;
                    handleLoginRequired();
                } else {
                    $.modal.msgError(res.msg);
                }
            }
        }
    });
}

// 处理取消收藏
function handleUncollect(type, targetId, onSuccess) {
    // 如果需要登录，直接返回
    if(handleLoginRequired()) {
        return;
    }
    
    $.ajax({
        url: ctx + "web/interaction/uncollect",
        type: "POST",
        data: {
            type: type,
            targetId: targetId
        },
        success: function(res) {
            if(res.code === 0) {
                $.modal.msgSuccess('已取消收藏');
                if(onSuccess) onSuccess(res);
            } else {
                // 如果返回未登录错误，更新登录状态并重新检查
                if(res.msg && res.msg.indexOf('登录') !== -1) {
                    window.isAuthenticated = false;
                    handleLoginRequired();
                } else {
                    $.modal.msgError(res.msg);
                }
            }
        }
    });
}