// 获取DOM元素
const dataContainer = document.getElementById('data-container');
const loadButton = document.getElementById('load-data');

// 数据加载函数
async function loadData() {
    try {
        // 显示加载状态
        dataContainer.innerHTML = '<p>数据加载中...</p>';

        // 发起异步请求
        const response = await fetch('/api/demo/data');
        
        if (!response.ok) {
            throw new Error('网络响应异常');
        }

        const data = await response.json();

        // 渲染数据
        dataContainer.innerHTML = `
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>名称</th>
                        <th>描述</th>
                    </tr>
                </thead>
                <tbody>
                    ${data.map(item => `
                        <tr>
                            <td>${item.id}</td>
                            <td>${item.name}</td>
                            <td>${item.description}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        // 错误处理
        dataContainer.innerHTML = `<p class="text-danger">数据加载失败：${error.message}</p>`;
        console.error('数据加载失败:', error);
    }
}

// 绑定点击事件
loadButton.addEventListener('click', loadData);

// 页面加载时自动加载数据
window.addEventListener('load', loadData);
