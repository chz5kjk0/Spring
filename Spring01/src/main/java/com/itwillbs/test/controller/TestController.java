package com.itwillbs.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// 컨트롤러 역할을 하는 클래스 정의 시 @Controller 어노테이션을 클래스 선언부 윗줄에 지정
@Controller
public class TestController {
	// "main" 서블릿 주소 요청 시 자동으로 호출되는 requestMain() 메서드 정의
	// => 파라미터 : 없음    리턴타입 : String
	// => @RequestMapping 어노테이션을 사용하여 "GET" 방식의 "main" 서블릿 주소 요청 받기
	@RequestMapping(value = "main", method = RequestMethod.GET )
	public String requestMain() {
		// Dispatch 방식으로 views 디렉토리 내의 "index.jsp" 페이지 요청
		// => web.xml 파일과 servlet-context.xml 파일의 설정으로 인해
		//    이동할 페이지의 파일명만 지정하면 
		//    디렉토리명("WEB-INF/views") 과 확장자(".jsp") 가 자동 결합됨
		return "index"; // "WEB-INF/views/index.jsp" 페이지로 이동하도록 Dispatch 요청 발생함
	}
	
	// "main2" 서블릿 주소 요청 시 자동으로 호출되는 requestMain2() 메서드 정의
	// => 파라미터 : 없음    리턴타입 : String
	// => @RequestMapping 어노테이션을 사용하여 "GET" 방식의 "main2" 서블릿 주소 요청 받기
	// => views 디렉토리의 main.jsp 페이지로 이동
	@RequestMapping(value = "main2", method = RequestMethod.GET)
	public String requestMain2() {
		return "main";
	}
	
	// "test1" 서블릿 주소 요청 처리할 test1() 메서드 정의
	// => @GetMapping 어노테이션을 통해 "GET" 방식 요청 처리
	@GetMapping(value = "test1")
	public String test1() {
		// views 디렉토리 내에 서브 디렉토리 사용 시
		// return "서브디렉토리명/파일명";
		return "test/test1";
	}
	
	@PostMapping(value = "test2")
	public String test2() {
		return "test/test2";
	}
}













