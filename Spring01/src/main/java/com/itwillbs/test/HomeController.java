package com.itwillbs.test;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// 컨트롤러 역할을 하는 클래스 정의 시 @Controller 어노테이션을 클래스 선언부 윗줄에 지정
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	// @RequestMapping 어노테이션을 사용하여 URL 매핑 작업 수행
	// => 파라미터 중 value 속성값에 지정된 URL 을 매핑 주소로 사용하며
	//    method 속성값에 지정된 RequestMethod.XXX 방식을 요청 메서드로 인식
	//    (이 때, method 는 복수개 지정이 가능하며 중괄호로 요청 방식을 묶어줌)
	//    (method = {RequestMethod.GET, RequestMethod.POST})
	// => 단, 스프링 4 버전부터 지원되는 @GetMapping, @PostMapping 어노테이션을 통해
	//    좀 더 간단하게 매핑이 가능하나 유연성은 떨어짐(자신에게 맞는 방식 사용)
	// => 형식 상 메서드를 정의하여 해당 URL 에 대해 자동으로 메서드가 호출되도록 정의
	//    => 메서드 파라미터 : 없을 수도 있고, 필요에 따라 전달받을 객체 타입 지정도 가능
	//       메서드 리턴타입 : String 또는 다른 타입 지정 가능
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		// Dispatch 방식으로 views 디렉토리 내의 "home.jsp" 페이지 요청
		// => web.xml 파일과 servlet-context.xml 파일의 설정으로 인해
		//    이동할 페이지의 파일명만 지정하면 
		//    디렉토리명("WEB-INF/views") 과 확장자(".jsp") 가 자동 결합됨
		return "home"; // "WEB-INF/views/home.jsp" 페이지로 이동하도록 Dispatch 요청 발생함
	}
	
}














