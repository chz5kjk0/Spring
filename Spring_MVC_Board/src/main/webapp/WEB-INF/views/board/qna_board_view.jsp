<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %> 
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %> 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>MVC 게시판</title>
<!-- 외부 CSS 가져오기 -->
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
<style type="text/css">
	#articleForm {
		width: 500px;
		height: 550px;
		border: 1px solid red;
		margin: auto;
	}
	
	h2 {
		text-align: center;
	}
	
	table {
		border: 1px solid black;
		border-collapse: collapse; 
	 	width: 500px;
	}
	
	th {
		text-align: center;
	}
	
	td {
		width: 150px;
		text-align: center;
	}
	
	#basicInfoArea {
		height: 70px;
		text-align: center;
	}
	
	#articleContentArea {
		background: orange;
		margin-top: 20px;
		height: 350px;
		text-align: center;
		overflow: auto;
		white-space: pre-line;
	}
	
	#commandList {
		margin: auto;
		width: 500px;
		text-align: center;
	}
</style>
</head>
<body>
	<header>
		<!-- Login, Join 링크 표시 영역 -->
		<jsp:include page="../inc/top.jsp"></jsp:include>
	</header>
	<!-- 게시판 상세내용 보기 -->
	<section id="articleForm">
		<h2>글 상세내용 보기</h2>
		<section id="basicInfoArea">
			<table border="1">
			<tr><th width="70">제 목</th><td colspan="3" >${board.board_subject }</td></tr>
			<tr>
				<th width="70">작성자</th><td>${board.board_name }</td>
				<th width="70">작성일</th>
				<td><fmt:formatDate value="${board.board_date }" pattern="yy-MM-dd HH:mm:SS" /></td>
			</tr>
			<tr>
				<th width="70">첨부파일</th>
				<td>
					<%--
					< JSTL Split >
					1. JSTL 의 split 기능 사용을 위해서 JSTL functions 라이브러리 추가 필요
					2. 분리할 문자열을 지정하여 대상 문자열 분리 후 변수에 저장
					   <c:set var="변수명x" value="${fn:split("대상문자열", "구분자")}" />
					3. 분리된 문자열을 반복문을 통해서 반복하여 접근
					   <c:forEach var="변수명y" items="변수명x">
					   		${변수명y}
					   </c:forEach>
					4. 실제 파일명에서 앞 부분 UUID 제거
					   1) 파일명 길이 알아내기(fn:length(문자열))
					   2) 파일명에서 원하는 문자가 있는 인덱스 알아내기
					      (fn:indexOf(대상문자열, 찾을문자열))
					   3) 파일명에서 UUID 를 제외한 문자열 추출하기
					      (fn:substring(대상문자열, 시작인덱스, 끝인덱스))
					      => 주의! 시작인덱스는 찾을문자열 기준 + 1
					--%>
					<c:set var="arrRealFile" value="${fn:split(board.board_real_file, '/') }"/>
					<c:forEach var="realFile" items="${arrRealFile }">
						<c:set var="nameLength" value="${fn:length(realFile) }" />
						<c:set var="indexOf_" value="${fn:indexOf(realFile, '_') }" />
						<c:set var="fileName" value="${fn:substring(realFile, indexOf_ + 1, nameLength) }" />
						<%-- 컨텍스트 경로/resources/upload 디렉토리 내의 파일 지정 --%>
						<a href="${pageContext.request.contextPath }/resources/upload/${realFile }" download="${fileName }">
							${fileName }<br>
						</a>
					</c:forEach>
				</td>
				<th width="70">조회수</th>
				<td>${board.board_readcount }</td>
			</tr>
			</table>
		</section>
		<section id="articleContentArea">
			${board.board_content }
		</section>
	</section>
	<section id="commandList">
		<input type="button" value="답변" onclick="location.href='BoardReplyForm.bo?board_num=${param.board_num}&pageNum=${param.pageNum }'">
		<input type="button" value="수정" onclick="location.href='BoardModifyForm.bo?board_num=${param.board_num}&pageNum=${param.pageNum }'">
		<input type="button" value="삭제" onclick="location.href='BoardDeleteForm.bo?board_num=${param.board_num}&pageNum=${param.pageNum }'">
		<input type="button" value="목록" onclick="location.href='BoardList.bo?pageNum=${param.pageNum}'">
	</section>
</body>
</html>
















