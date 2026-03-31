// 课程练习功能 JavaScript

// 提交答案
function submitAnswer(exerciseId, correctAnswer) {
    var answerInput = $('input[name="answer_' + exerciseId + '"]:checked');
    var textareaInput = $('textarea[name="answer_' + exerciseId + '"]');
    var userAnswer = '';
    
    if (answerInput.length > 0) {
        userAnswer = answerInput.val();
    } else if (textareaInput.length > 0 && textareaInput.val().trim() !== '') {
        userAnswer = textareaInput.val().trim();
    } else {
        $.modal.alertWarning('请先选择或填写答案');
        return;
    }
    
    if (userAnswer === correctAnswer) {
        $.modal.alertSuccess('恭喜你，回答正确！');
    } else {
        $.modal.alertWarning('回答错误，请查看答案再试一次');
    }
    
    showAnswer(exerciseId);
}

// 查看答案
function showAnswer(exerciseId) {
    var answerDiv = $('#answer-' + exerciseId);
    if (answerDiv.is(':visible')) {
        answerDiv.hide();
    } else {
        answerDiv.show();
        $('html, body').animate({
            scrollTop: answerDiv.offset().top - 200
        }, 300);
    }
}
