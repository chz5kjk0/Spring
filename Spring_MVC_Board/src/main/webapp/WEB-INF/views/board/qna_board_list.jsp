<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>  
<%-- EL 에서 표기 방식(날짜 등)을 변경하려면 fmt(format) 라이브러리 필요  --%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>  
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>MVC 게시판</title>
<!-- 외부 CSS 가져오기 -->
<link href="${pageContext.request.contextPath}/resources/css/default.css" rel="stylesheet" type="text/css">
<style type="text/css">
	#listForm {
		width: 1024px;
		max-height: 610px;
		margin: auto;
	}
	
	h2 {
		text-align: center;
	}
	
	table {
		margin: auto;
		width: 1024px;
	}
	
	#tr_top {
		background: orange;
		text-align: center;
	}
	
	table td {
		text-align: center;
	}
	
	#subject {
		text-align: left;
		padding-left: 20px;
	}
	
	#pageList {
		margin: auto;
		width: 1024px;
		text-align: center;
	}
	
	#emptyArea {
		margin: auto;
		width: 1024px;
		text-align: center;
	}
	
	#buttonArea {
		margin: auto;
		width: 1024px;
		text-align: right;
		margin-top: 10px;
	}
	
	a {
		text-decoration: none;
	}
</style>
<script src="${pageContext.request.contextPath }/resources/js/jquery-3.6.3.js"></script>
<script type="text/javascript">
	// AJAX 를 활용한 게시물 목록 표시에 사용될 페이지 번호값 미리 저장
	let pageNum = 1;
	
	$(function() {
		// 검색타입(searchType)과 검색어(keyword) 값 가져와서 변수에 저장
		let searchType = $("#searchType").val();
		let keyword = $("#keyword").val();
// 		alert(searchType + ", " + keyword);
		
		// 게시물 목록 조회를 처음 수행하기 위해 load_list() 함수 호출
		// => 검색타입과 검색어를 파라미터로 전달(검색하지 않을 경우에도 동일) 
		load_list(searchType, keyword);
		
		// 무한스크롤 기능 구현
		// window 객체에서 scroll 동작 시 기능 수행(이벤트 처리)을 위해 scroll() 함수 호출
		$(window).scroll(function() {
// 			$("#listForm").before("확인");
			// 1. window 객체와 document 객체를 활용하여 스크롤 관련 값 가져오기
			// => 스크롤바 현재 위치, 문서 표시되는 창의 높이, 문서 전체 높이
			let scrollTop = $(window).scrollTop();
			let windowHeight = $(window).height();
			let documentHeight = $(document).height();
			
// 			console.log("scrollTop : " + scrollTop + ", windowHeight : " + windowHeight + ", documentHeight : " + documentHeight + "<br>");

			// 2. 스크롤바 위치값 + 창 높이 + x 가 문서 전체 높이 이상이면
			//    다음 페이지 게시물 목록 로딩하여 추가
			// => 이 때, x 값은 마지막으로부터 여유 공간으로 둘 스크롤바 아래쪽 남은 공간(픽셀값)
			//    (x 값을 1로 지정 시 스크롤바가 바닥에 닿을 때 다음 페이지 로딩)
			if(scrollTop + windowHeight + 1 >= documentHeight) {
				// 다음 페이지 로딩하기 위한 load_list() 함수 호출
				// => 이 때, 페이지 번호를 1 증가시켜 다음 페이지 목록 로딩
				pageNum++;
				load_list(searchType, keyword);
			}
		});
	});
	
	// 게시물 목록 조회를 AJAX + JSON 으로 처리할 load_list() 함수 정의
	// => 검색타입과 검색어를 파라미터로 지정
	function load_list(searchType, keyword) {
		$.ajax({
			type: "GET",
// 			url: "BoardListJson.bo?pageNum=" + pageNum,
			url: "BoardListJson.bo?pageNum=" + pageNum + "&searchType=" + searchType + "&keyword=" + keyword,
			dataType: "json"
		})
		.done(function(boardList) { // 요청 성공 시
// 			$("#listForm > table").append(boardList);
			
			// JSONArray 객체를 통해 배열 형태로 전달받은 JSON 데이터를
			// 반복문을 통해 하나씩 접근하여 객체 꺼내기
			for(let board of boardList) {
				// 테이블에 표시할 JSON 데이터 출력문 생성
				// => 출력할 데이터는 board.xxx 형식으로 접근
				let result = "<tr height='100'>"
							+ "<td>" + board.board_num + "</td>"
							+ "<td id='subject'>" 
								+ "<a href='BoardDetail.bo?board_num=" + board.board_num + "'>"
								+ board.board_subject + "</a></td>"
							+ "<td>" + board.board_name + "</td>"
							+ "<td>" + board.board_date + "</td>"
							+ "<td>" + board.board_readcount + "</td>"
							+ "</tr>";
				
				// 지정된 위치(table 태그 내부)에 JSON 객체 출력문 추가
				$("#listForm > table").append(result);
			}
		})
		.fail(function() {
			$("#listForm > table").append("<h3>요청 실패!</h3>");
		});
	}
</script>
</head>
<body>
	<header>
		<!-- Login, Join 링크 표시 영역 -->
		<jsp:include page="../inc/top.jsp"></jsp:include>
	</header>
	<!-- 게시판 리스트 -->
	<section id="listForm">
		<h2>게시판 글 목록</h2>
		<section id="buttonArea">
			<form action="BoardList.bo">
				<!-- 검색 타입 추가 -->
				<select name="searchType" id="searchType">
					<option value="subject" <c:if test="${param.searchType eq 'subject'}">selected</c:if>>제목</option>
					<option value="content" <c:if test="${param.searchType eq 'content'}">selected</c:if>>내용</option>
					<option value="subject_content" <c:if test="${param.searchType eq 'subject_content'}">selected</c:if>>제목&내용</option>
					<option value="name" <c:if test="${param.searchType eq 'name'}">selected</c:if>>작성자</option>
				</select>
				<input type="text" name="keyword" id="keyword" value="${param.keyword }">
				<input type="submit" value="검색">
				&nbsp;&nbsp;
				<input type="button" value="글쓰기" onclick="location.href='BoardWriteForm.bo'" />
			</form>
		</section>
		<table>
			<tr id="tr_top">
				<td width="100px">번호</td>
				<td>제목</td>
				<td width="150px">작성자</td>
				<td width="150px">날짜</td>
				<td width="100px">조회수</td>
			</tr>
			<!-- AJAX 를 사용하여 글목록 조회 결과를 표시할 위치 -->
		</table>
	</section>
</body>
</html>













