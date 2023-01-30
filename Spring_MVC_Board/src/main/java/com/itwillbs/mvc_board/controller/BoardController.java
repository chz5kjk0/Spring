package com.itwillbs.mvc_board.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.itwillbs.mvc_board.service.BoardService;
import com.itwillbs.mvc_board.vo.BoardVO;
import com.itwillbs.mvc_board.vo.PageInfo;

@Controller
public class BoardController {
	@Autowired
	private BoardService service;
	
	@GetMapping("/BoardWriteForm.bo")
	public String write(Model model, HttpSession session) {
		// 세션 아이디가 존재하지 않으면 "로그인 필수!" 출력하고 이전페이지로 이동시키기
		String sId = (String)session.getAttribute("sId");
		if(sId == null || sId.equals("")) {
			model.addAttribute("msg", "로그인 필수!");
			return "fail_back";
		}
		
		return "board/qna_board_write";
	}
	
	// 글쓰기 비즈니스 로직 - 파일 업로드 기능 추가
	// => commons-io, commons-fileupload 라이브러리 추가
	// => servlet-context.xml 에 CommonsMultipartResolver 객체 설정 추가
	// => 주의! servlet-context.xml 에서 파일 크기 제한 시 파일 크기 초과하면
	//    org.apache.commons.fileupload.FileUploadBase$SizeLimitExceededException 예외 발생하므로
	//    파일 용량 초과에 대한 처리 추가 필요함(지금은 생략)
	@PostMapping("/BoardWritePro.bo")
	public String writePro(@ModelAttribute BoardVO board, Model model, HttpSession session) {
//		System.out.println(board);
		// 세션 아이디가 존재하지 않으면 "로그인 필수!" 출력하고 이전페이지로 이동시키기
		String sId = (String)session.getAttribute("sId");
		if(sId == null || sId.equals("")) {
			model.addAttribute("msg", "로그인 필수!");
			return "fail_back";
		}
		
		// -------------------------------------------------------------------------
		// 주의! 파일 업로드 기능을 통해 전달받은 객체를 다룰 때
		// MultipartFile 타입 클래스 활용할 경우
		// BoardVO 클래스 내에 MultipartFile 타입 변수와 Getter/Setter 정의 필수
		// => 이 때, 변수명은 input type="file" 태그의 name 속성과 동일한 변수명 사용
		// -------------------------------------------------------------------------
		// 1. 가상 업로드 경로에 대한 실제 업로드 경로 알아내기
		// => 단, request.getServletContext() 메서드 대신 session 객체로 동일한 작업 수행 가능
		String uploadDir = "/resources/upload"; // 가상의 업로드 경로(루트(webapp) 기준)
		// => webapp 디렉토리 내의 resources 디렉토리에 upload 디렉토리 생성 필요
		String saveDir = session.getServletContext().getRealPath(uploadDir);
//		System.out.println("실제 업로드 경로 : " + saveDir);
		
		// -------------------------------------------------------------------------------
		// ---------------------------- java.io.File 객체 활용 ---------------------------
//		// 실제 경로를 갖는 File 객체 생성
//		File f = new File(saveDir);
//		// 만약, 해당 경로 상에 실제 디렉토리(폴더)가 존재하지 않을 경우 새로 생성
//		if(!f.exists()) {
////			f.mkdir(); // 최종 경로가 존재하지 않으면 생성
//			f.mkdirs(); // 지정된 경로 상에 존재하지 않는 모든 경로를 차례대로 생성
//		}
		// --------------- java.nio 패키지(Files, Path, Paths) 객체 활용 -----------------
		// 1. Paths.get() 메서드를 호출하여 대상 파일 또는 경로에 대한 Path 객체 얻어오기
		Path path = Paths.get(saveDir);
		// 2. Files 클래스의 createDirectories() 메서드를 호출하여
		//    지정된 경로 또는 파일 생성하기
		try {
			Files.createDirectories(path);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// -------------------------------------------------------------------------------
		
		// BoardVO 객체에 전달된 MultipartFile 객체 꺼내기
		// => 단, 복수개의 파라미터가 동일한 name 속성으로 전달된 경우 배열 타입으로 처리
		MultipartFile[] mFiles = board.getFiles();
		// 만약, 단일 파일일 경우
//		MultipartFile mFile = board.getFile();
		
		// 복수개의 파일을 하나의 이름으로 묶어서 다룰 경우에 사용할 변수 선언
		String originalFileNames = "";
		String realFileNames = "";
		
		// 복수개의 파일에 접근하기 위한 반복문
		for(MultipartFile mFile : mFiles) {
			// MultipartFile 객체의 getOriginalFilename() 메서드를 통해 파일명 꺼내기
			String originalFileName = mFile.getOriginalFilename();
//			long fileSize = mFile.getSize();
//			System.out.println("원본 파일명 : " + originalFileName);
//			System.out.println("파일크기 : " + fileSize + " Byte");
			
			// 가져온 파일이 있을 경우에만 중복 방지 대책 수행하기
			if(!originalFileName.equals("")) {
				// 파일명 중복 방지 대책
				// 시스템에서 랜덤ID 값을 추출하여 파일명 앞에 붙이기("랜덤ID값_파일명" 형식)
				// => 랜덤ID 값 추출은 UUID 클래스 활용(범용 고유 식별자)
				String uuid = UUID.randomUUID().toString();
	//			System.out.println("업로드 될 파일명 : " + uuid + "_" + originalFileName);
				
				// 파일명을 결합하여 보관할 변수에 하나의 파일 문자열 결합
				originalFileNames += originalFileName + "/";
				realFileNames += uuid + "_" + originalFileName + "/";
			} else {
				// 파일이 존재하지 않을 경우 널스트링으로 대체(뒤에 슬래시 포함)
				originalFileNames += "/";
				realFileNames += "/";
			}
		}
		
		// BoardVO 객체에 원본 파일명과 업로드 될 파일명 저장
		board.setBoard_file(originalFileNames);
		board.setBoard_real_file(realFileNames);
		System.out.println("원본 파일명 : " + board.getBoard_file());
		System.out.println("업로드 될 파일명 : " + board.getBoard_real_file());
		// => 실제로는 UUID 를 결합한 파일명만 사용하여 원본파일명과 실제파일명 모두 처리 가능
		// => 실제파일명에서 가장 먼저 만나는 "_" 기호를 기준으로 분리하면
		//    두번째 배열 인덱스 데이터가 원본 파일명이 된다!
		
		// --------------------------------------------------------------------
		// Service 객체의 registBoard() 메서드를 호출하여 게시물 등록 작업 요청
		// => 파라미터 : BoardVO 객체    리턴타입 : int(insertCount)
		int insertCount = service.registBoard(board);
		
		// 등록 성공/실패에 따른 포워딩 작업 수행
		if(insertCount > 0) { // 성공
			try {
				// 주의! 파일 등록 작업 성공 후 반드시 실제 폴더 위치에 업로드 수행 필요!
				// => MultipartFile 객체는 임시 경로에 파일을 업로드하므로
				//    작업 성공 시 transferTo() 메서드를 호출하여 실제 위치로 이동 작업 필요
				//    (파라미터 : new File(업로드경로, 업로드파일명)
				// MultipartFile 배열 크기만큼 반복 
				for(int i = 0; i < board.getFiles().length; i++) {
					// 하나씩 배열에서 객체 꺼내기
					MultipartFile mFile = board.getFiles()[i];
//					System.out.println("MultipartFile : " + mFile.getOriginalFilename());
					
					// 가져온 파일이 있을 경우에만 파일 이동 작업 수행
					if(!mFile.getOriginalFilename().equals("")) {
						// 실제 파일명을 "/" 기준으로 분리하여 
						// 배열 인덱스와 동일한 위치의 문자열을 이동할 파일명으로 사용
						mFile.transferTo(
							new File(saveDir, board.getBoard_real_file().split("/")[i])
						);
					}
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// 글 목록 페이지(BoardList.bo) 로 리다이렉트
			return "redirect:/BoardList.bo";
		} else { // 실패
			// "msg" 속성명으로 "글 쓰기 실패!" 메세지 전달 후 fail_back 포워딩
			model.addAttribute("msg", "글 쓰기 실패!");
			return "fail_back";
		}
		
	}
	
	// "/BoardList.bo" 서블릿 요청에 대한 글 목록 조회 비즈니스 로직 list() - GET
	// => 파라미터 : 검색타입(searchType) => 기본값 널스트링
	//               검색어(keyword) => 기본값 널스트링
	//               현재 페이지번호(pageNum) => 기본값 1 로 설정
	//               검색어(keyword) => 기본값 널스트링
	//               데이터 저장 Model 객체(model)
//	@GetMapping("/BoardList.bo")
//	public String list(
//			@RequestParam(defaultValue = "") String searchType,
//			@RequestParam(defaultValue = "") String keyword,
//			@RequestParam(defaultValue = "1") int pageNum,
//			Model model) {
		// ---------------------------------------------------------------------------
//		// 페이징 처리를 위한 변수 선언
//		int listLimit = 10; // 한 페이지에서 표시할 게시물 목록을 10개로 제한
//		int startRow = (pageNum - 1) * listLimit; // 조회 시작 행번호 계산
////		System.out.println("startRow = " + startRow);
//		// ---------------------------------------------------------------------------
//		// Service 객체의 getBoardList() 메서드를 호출하여 게시물 목록 조회
//		// => 파라미터 : 검색타입, 검색어, 시작행번호, 목록갯수   
//		// => 리턴타입 : List<BoardVO>(boardList)
//		List<BoardVO> boardList = service.getBoardList(searchType, keyword, startRow, listLimit);
//		// ---------------------------------------------------------------------------
//		// 페이징 처리
//		// 한 페이지에서 표시할 페이지 목록(번호) 갯수 계산
//		// 1. Service 객체의 selectBoardListCount() 메서드를 호출하여 전체 게시물 수 조회
//		// => 파라미터 : 검색타입, 검색어   리턴타입 : int(listCount)
//		int listCount = service.getBoardListCount(searchType, keyword);
////		System.out.println("총 게시물 수 : " + listCount);
//		
//		// 2. 한 페이지에서 표시할 페이지 목록 갯수 설정
//		int pageListLimit = 10; // 한 페이지에서 표시할 페이지 목록을 3개로 제한
//		
//		// 3. 전체 페이지 목록 수 계산
//		int maxPage = listCount / listLimit 
//						+ (listCount % listLimit == 0 ? 0 : 1); 
//		
//		// 4. 시작 페이지 번호 계산
//		int startPage = (pageNum - 1) / pageListLimit * pageListLimit + 1;
//		
//		// 5. 끝 페이지 번호 계산
//		int endPage = startPage + pageListLimit - 1;
//		
//		// 6. 만약, 끝 페이지 번호(endPage)가 전체(최대) 페이지 번호(maxPage) 보다
//		//    클 경우, 끝 페이지 번호를 최대 페이지 번호로 교체
//		if(endPage > maxPage) {
//			endPage = maxPage;
//		}
//		
//		// PageInfo 객체 생성 후 페이징 처리 정보 저장
//		PageInfo pageInfo = new PageInfo(listCount, pageListLimit, maxPage, startPage, endPage);
//		// ---------------------------------------------------------------------------
//		// 게시물 목록 객체(boardList) 와 페이징 정보 객체(pageInfo)를 Model 객체에 저장
//		model.addAttribute("boardList", boardList);
//		model.addAttribute("pageInfo", pageInfo);
//		
//		return "board/qna_board_list";
//	}
	
	// ===============================================================================
	// 기본 목록 뷰페이지로 이동하는 서블릿 처리
	@GetMapping("/BoardList.bo")
	public String list() {
		return "board/qna_board_list";
	}
	
	// AJAX 요청을 통한 글목록 조회
	// => AJAX 요청에 대한 JSON 데이터로 응답
	// => 현재 메서드에서 JSON 타입 응답 데이터를 바로 생성하여 출력하기 위해
	//    @ResponseBody 어노테이션 필요
	// => 이동할 페이지가 없으므로 리턴타입 void
	// => 만약, 응답 데이터의 한글이 깨질 경우 다음과 같이 매핑 데이터에 정보 추가
	//    @GetMapping(value = "/BoardListJson.bo", produces = "application/json; charset=utf-8")
	@ResponseBody
	@GetMapping("/BoardListJson.bo")
	public void listJson(
			@RequestParam(defaultValue = "") String searchType,
			@RequestParam(defaultValue = "") String keyword,
			@RequestParam(defaultValue = "1") int pageNum,
			Model model,
			HttpServletResponse response) {
		
		// 페이징 처리를 위한 변수 선언
		int listLimit = 10; // 한 페이지에서 표시할 게시물 목록을 10개로 제한
		int startRow = (pageNum - 1) * listLimit; // 조회 시작 행번호 계산
		// Service 객체의 getBoardList() 메서드를 호출하여 게시물 목록 조회
		// ---------------------------------------------------------------------------
		// => 파라미터 : 검색타입, 검색어, 시작행번호, 목록갯수   
		// => 리턴타입 : List<BoardVO>(boardList)
		List<BoardVO> boardList = service.getBoardList(searchType, keyword, startRow, listLimit);
		// ---------------------------------------------------------------------------
		// 자바 데이터를 JSON 형식으로 변환하기
		// => org.json 패키지의 JSONObject 클래스를 활용하여 JSON 객체 1개를 생성하고
		//    JSONArray 클래스를 활용하여 JSONObject 객체 복수개에 대한 배열 생성
		// 0. JSONObject 객체 복수개를 저장할 JSONArray 클래스 인스턴스 생성
		JSONArray jsonArray = new JSONArray();
		
		// 1. List 객체 크기만큼 반복
		for(BoardVO board : boardList) {
			// 2. JSONObject 클래스 인스턴스 생성
			// => 파라미터 : VO(Bean) 객체(멤버변수 및 Getter/Setter, 기본생성자 포함)
			JSONObject jsonObject = new JSONObject(board);
//			System.out.println(jsonObject);
			
			// 참고. 저장되어 있는 JSON 데이터를 꺼낼 수도 있다! - get() 메서드 활용
//			System.out.println(jsonObject.get("board_pass"));
			
			// 3. JSONArray 객체의 put() 메서드를 호출하여 JSONObject 객체 추가
			jsonArray.put(jsonObject);
		}
		
//		System.out.println(jsonArray);
		// => JSONObject 복수개가 배열 형태로 JSONArray 객체에 저장되어 있음
		// 또한, JSONArray 객체에서 JSONObject 객체를 꺼낼 수도 있다!
//		JSONObject jsonObject = (JSONObject)jsonArray.get(0); // 첫번째 배열에서 꺼내기
		// => 이 때, 리턴타입이 Object 타입이므로 JSONObject 타입 형변환 필요
//		System.out.println(jsonObject.get("board_pass"));
		
		try {
			// 생성된 JSON 객체를 활용하여 응답 데이터를 직접 생성 후 웹페이지에 출력
			// response 객체의 setCharacterEncoding() 메서드로 출력 데이터 인코딩 지정 후
			// response 객체의 getWriter() 메서드로 PrintWriter 객체를 리턴받아
			// PrintWriter 객체의 print() 메서드를 호출하여 응답데이터 출력
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(jsonArray); // toString() 생략됨
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	// ===============================================================================
	
	@GetMapping(value = "/BoardDetail.bo")
	public String detail(@RequestParam int board_num, Model model) {
		// Service 객체의 getBoard() 메서드를 호출하여 게시물 상세 정보 조회
		// => 파라미터 : 글번호    리턴타입 : BoardVO(board)
		BoardVO board = service.getBoard(board_num);
		System.out.println(board);
		// 조회 결과 BoardVO 객체가 존재할 경우 조회수 증가 - increaseReadcount()
		// => 파라미터 : 글번호
		if(board != null) {
			service.increaseReadcount(board_num);
			
			// 조회수 증가 후 BoardVO 객체의 조회수(board_readcount) 값 1 증가시키기
			board.setBoard_readcount(board.getBoard_readcount() + 1);
		}
		
		// Model 객체에 BoardVO 객체 추가
		model.addAttribute("board", board);
		
		return "board/qna_board_view";
	}
	
	// BoardDeleteForm.bo 요청에 대한 글 삭제 폼 요청 - GET
	@GetMapping(value = "/BoardDeleteForm.bo")
	public String delete() {
		return "board/qna_board_delete";
	}
	
	// BoardDeletePro.bo 요청에 대한 글 삭제 비즈니스 로직 - POST
	@PostMapping(value = "/BoardDeletePro.bo")
	public String deletePro(@ModelAttribute BoardVO board, 
			@RequestParam(defaultValue = "1") int pageNum, Model model, HttpSession session) {
		// Service 객체의 isBoardWriter() 메서드를 호출하여 
		// 전달받은 패스워드가 게시물의 패스워드와 일치하는지 비교
		// => 파라미터 : 글번호, 패스워드    리턴타입 : BoardVO
		if(service.isBoardWriter(board.getBoard_num(), board.getBoard_pass()) != null) { // 일치
//			System.out.println("패스워드 일치!");
			
			// 주의! 삭제 전 해당 게시물의 파일명 조회 위해
			// Service 객체의 getRealFile() 메서드를 호출
			// => 파라미터 : 글번호    리턴타입 : String(realFile)
			String realFile = service.getRealFile(board.getBoard_num());
//			System.out.println(realFile);
			
			// Service 객체의 removeBoard() 메서드를 호출하여 게시물 삭제 작업 요청
			// => 파라미터 : 글번호    리턴타입 : int(deleteCount)
			int deleteCount = service.removeBoard(board.getBoard_num());
			
			// 게시물 삭제 성공 시 해당 게시물의 파일도 삭제
			if(deleteCount > 0) { // 삭제 성공
				// 파일명을 "/" 문자열 기준으로 분리 후 for문 반복을 통해 해당 파일 삭제
//				String[] arrRealFile = realFile.split("/");
//				for(String fileName : arrRealFile) {
//					System.out.println(fileName);
//				}
				
				for(String fileName : realFile.split("/")) {
					String uploadDir = "/resources/upload"; // 가상의 업로드 경로(루트(webapp) 기준)
					String saveDir = session.getServletContext().getRealPath(uploadDir);
					
//					File f = new File(saveDir, fileName);
//					// 해당 파일이 존재할 경우 삭제
//					if(f.exists()) {
//						f.delete();
//					}
					// --------------- java.nio 패키지(Files, Path, Paths) 객체 활용 -----------------
					// 1. Paths.get() 메서드를 호출하여 대상 파일에 대한 Path 객체 얻어오기
					Path path = Paths.get(saveDir + "/" + fileName);
					// 2. Files 클래스의 deleteIfExists() 메서드를 호출하여 지정된 파일 삭제하기
					try {
						Files.deleteIfExists(path);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					// -------------------------------------------------------------------------------
				}
				
				return "redirect:/BoardList.bo?pageNum=" + pageNum;
			} else { // 삭제 실패
				model.addAttribute("msg", "게시물 삭제 실패!");
				return "fail_back";
			}
			
		} else { // 불일치
			model.addAttribute("msg", "삭제 권한이 없습니다!");
			return "fail_back";
		}
		
	}
	
	
}















