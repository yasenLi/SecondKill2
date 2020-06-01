<%--
  Created by IntelliJ IDEA.
  User: senbition
  Date: 2020/5/28
  Time: 16:53
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>phone</title>
    <script type="text/javascript" src="jquery/jquery-2.1.1.min.js"></script>
</head>
<body>
<form method="post" action="checkVal">
    请输入手机号：<input id="phone" type="text" name="phone">
    <button class="getCode" type="button">获取验证码</button><br>
    请输入验证码: <input id="code" type="text" name="code">
    <span class="errorMsg" style="color: red"></span><br>
    <button class="checkVal" type="submit">提交</button>
</form>
<script type="text/javascript">
    $(".getCode").click(function () {
        var phone = $("#phone").val();
        $.ajax({
            type:"post",
            url:"http://localhost:8080/SecondKill/getCode",
            data:{phone,phone},
            success:function (res) {
                alert(res);
                if(res=="ok"){
                    $(".errorMsg").append("验证码发送成功");
                }else{
                    $(".errorMsg").append(res);
                }
            }
        });

    });
    $(".checkVal").click(function () {
        $.ajax({
            type:"post",
            url:$("form").prop("action"),
            data:$("form").serialize(),
            success:function (res) {
                if(res=="ok"){
                    $(".errorMsg").append("验证成功");
                }else{
                    $(".errorMsg").append(res);
                }
            }
        });
        return false;
    });
</script>
</body>
</html>
