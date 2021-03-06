package site.metacoding.blogv3.web;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.config.auth.LoginUser;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.service.PostService;
import site.metacoding.blogv3.web.dto.love.LoveRespDto;
import site.metacoding.blogv3.web.dto.post.PostDetailRespDto;
import site.metacoding.blogv3.web.dto.post.PostRespDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@RequiredArgsConstructor
@Controller
public class PostController {

    private final PostService postService;

    // /s/api/post/{id}/love
    @PostMapping("/s/api/post/{postId}/love")
    public ResponseEntity<?> love(@PathVariable Integer postId, @AuthenticationPrincipal LoginUser loginUser) {
        // 어떤 포스트를 누가 좋아요했는가?
        LoveRespDto loveRespDto = postService.좋아요(postId, loginUser.getUser());
        return new ResponseEntity<>(loveRespDto, HttpStatus.CREATED); // body에 저장된 데이터를 주지않으면 나중에 좋아요 취소를 할 수 없음
    }

    @DeleteMapping("/s/api/post/{postId}/love/{loveId}")
    public ResponseEntity<?> unLove(@PathVariable Integer loveId, @AuthenticationPrincipal LoginUser loginUser) {
        // 어떤 포스트를 누가 좋아요 취소했는가?
        // 로그인한 유저의 userId 러브에 있는 userId 비교
        postService.좋아요취소(loveId, loginUser.getUser());
        return new ResponseEntity<>(HttpStatus.OK); // 다시눌릴땐 어차피 다시 insert 될거라 body 필요없음
    }

    @DeleteMapping("/s/api/post/{id}")
    public ResponseEntity<?> postDelete(@PathVariable Integer id, @AuthenticationPrincipal LoginUser loginUser) {
        User principal = loginUser.getUser();
        postService.게시글삭제(id, principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/post/{id}")
    public String detail(@PathVariable Integer id, Model model, @AuthenticationPrincipal LoginUser loginUser) {
        PostDetailRespDto postDetailRespDto = null;

        if (loginUser == null) {
            postDetailRespDto = postService.게시글상세보기(id);
        } else {
            postDetailRespDto = postService.게시글상세보기(id, loginUser.getUser());
        }

        model.addAttribute("data", postDetailRespDto);

        return "/post/detail";
    }

    @PostMapping("/s/post")
    public String write(PostWriteReqDto postWriteReqDto, @AuthenticationPrincipal LoginUser loginUser) {
        postService.게시글쓰기(postWriteReqDto, loginUser.getUser());
        return "redirect:/user/" + loginUser.getUser().getId() + "/post";

        // LoginUser 테스트 시작

        // System.out.println("========================================================");
        // System.out.println(loginUser.getUsername());
        // System.out.println(loginUser.getUser().getId());
        // System.out.println("========================================================");

        // postWriteReqDto 테스트 완료!!

        // if (postWriteReqDto.getTitle() == null) {
        // throw new NullPointerException("title이 없습니다.");
        // }
        // if (postWriteReqDto.getContent() == null) {
        // throw new NullPointerException("content가 없습니다.");
        // }
        // if (postWriteReqDto.getCategoryId() == null) {
        // throw new NullPointerException("categoryId가 없습니다.");
        // }
        // return "1";
    }

    @GetMapping("/s/post/write-form")
    public String writeForm(@AuthenticationPrincipal LoginUser loginUser, Model model) {
        List<Category> categories = postService.게시글쓰기화면(loginUser.getUser());

        if (categories.size() == 0) {
            throw new CustomException("카테고리 등록이 필요합니다.");
        }

        model.addAttribute("categories", categories);
        return "/post/writeForm";
    }

    @GetMapping("/s/post/{postId}")
    public String updateForm(@PathVariable Integer postId, @AuthenticationPrincipal LoginUser loginUser, Model model) {
        PostDetailRespDto postDetailRespDto = postService.게시글상세보기(postId, loginUser.getUser());
        model.addAttribute("data", postDetailRespDto);
        return "/post/updateForm";
    }

    @PutMapping("/s/post/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal LoginUser loginUser, Model model) {

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // post, category 다 같이 가지고 가야하니까 categoryService 사용하지 말고 postService 사용하기
    @GetMapping("/user/{pageOwnerId}/post")
    public String postList(Integer categoryId, @PathVariable Integer pageOwnerId,
            @AuthenticationPrincipal LoginUser loginUser,
            Model model,
            @PageableDefault(size = 3) Pageable pageable) {

        PostRespDto postRespDto = null;

        if (categoryId == null) {
            postRespDto = postService.게시글목록보기(pageOwnerId, pageable);
        } else {
            postRespDto = postService.카테고리별게시글목록보기(pageOwnerId, categoryId, pageable);
        }

        model.addAttribute("postRespDto", postRespDto);

        return "/post/list";
    }

}
