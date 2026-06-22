package com.rewe.studentstrackingsystem.web.controller;

import com.rewe.studentstrackingsystem.course.dto.CourseRequest;
import com.rewe.studentstrackingsystem.course.services.CourseService;
import com.rewe.studentstrackingsystem.exception.InvalidOperationException;
import com.rewe.studentstrackingsystem.user.dto.UserRequest;
import com.rewe.studentstrackingsystem.user.dto.UserUpdateRequest;
import com.rewe.studentstrackingsystem.user.services.UserService;
import com.rewe.studentstrackingsystem.teacher.services.TeacherService;
import com.rewe.studentstrackingsystem.web.model.CourseCreateForm;
import com.rewe.studentstrackingsystem.web.model.ProfileUpdateForm;
import com.rewe.studentstrackingsystem.web.model.RegistrationForm;
import com.rewe.studentstrackingsystem.web.services.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PageController {

    private static final String INDEX_VIEW = "index";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String MODE_LOGIN = "login";
    private static final String MODE_REGISTER = "register";

    private final UserService userService;
    private final DashboardService dashboardService;
    private final TeacherService teacherService;
    private final CourseService courseService;

    @GetMapping("/")
    public ModelAndView index(
            Authentication authentication,
            @RequestParam(value = "error", required = false) String loginError,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "registrationError", required = false) String registrationError,
            @RequestParam(value = "mode", required = false, defaultValue = MODE_LOGIN) String mode
    ) {
        if (isAuthenticated(authentication)) {
            return new ModelAndView("redirect:/home");
        }

        var modelAndView = new ModelAndView(INDEX_VIEW);
        modelAndView.addObject("registrationForm", new RegistrationForm(null, null, null, null, null, null, null));
        modelAndView.addObject("availableRoles", List.of(ROLE_STUDENT, ROLE_TEACHER));
        modelAndView.addObject("activeMode", normalizeMode(mode));
        modelAndView.addObject("loginError", loginError != null);
        modelAndView.addObject("loggedOut", logout != null);
        modelAndView.addObject("registered", registered != null);
        modelAndView.addObject("registrationError", registrationError != null);
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView register(
            @Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            var modelAndView = new ModelAndView(INDEX_VIEW);
            modelAndView.addObject("availableRoles", List.of(ROLE_STUDENT, ROLE_TEACHER));
            modelAndView.addObject("activeMode", MODE_REGISTER);
            modelAndView.addObject("registrationError", true);
            modelAndView.addObject("registrationErrorMessage", firstBindingError(bindingResult));
            return modelAndView;
        }

        try {
            userService.save(new UserRequest(
                    registrationForm.username(),
                    registrationForm.password(),
                    registrationForm.role(),
                    registrationForm.firstName(),
                    registrationForm.lastName(),
                    registrationForm.email(),
                    registrationForm.profilePictureUrl()));
        } catch (InvalidOperationException ex) {
            var modelAndView = new ModelAndView(INDEX_VIEW);
            modelAndView.addObject("availableRoles", List.of(ROLE_STUDENT, ROLE_TEACHER));
            modelAndView.addObject("activeMode", MODE_REGISTER);
            modelAndView.addObject("registrationError", true);
            modelAndView.addObject("registrationErrorMessage", ex.getMessage());
            return modelAndView;
        } catch (DataIntegrityViolationException ex) {
            var modelAndView = new ModelAndView(INDEX_VIEW);
            modelAndView.addObject("availableRoles", List.of(ROLE_STUDENT, ROLE_TEACHER));
            modelAndView.addObject("activeMode", MODE_REGISTER);
            modelAndView.addObject("registrationError", true);
            modelAndView.addObject("registrationErrorMessage", "Username or email already exists.");
            return modelAndView;
        }

        return new ModelAndView("redirect:/?registered");
    }

    private String normalizeMode(String mode) {
        if (MODE_REGISTER.equalsIgnoreCase(mode)) {
            return MODE_REGISTER;
        }
        return MODE_LOGIN;
    }

    private String firstBindingError(BindingResult bindingResult) {
        var error = bindingResult.getFieldError();
        if (error != null && error.getDefaultMessage() != null && !error.getDefaultMessage().isBlank()) {
            return error.getDefaultMessage();
        }
        return "Please complete all required fields correctly.";
    }

    @GetMapping("/home")
    public ModelAndView home(@AuthenticationPrincipal UserDetails userDetails) {
        var dashboard = dashboardService.getDashboard(userDetails.getUsername());
        return viewWithCommonModel("home", dashboard);
    }

    @GetMapping("/grades")
    public ModelAndView grades(@AuthenticationPrincipal UserDetails userDetails) {
        var dashboard = dashboardService.getDashboard(userDetails.getUsername());
        return viewWithCommonModel("grades", dashboard);
    }

    @GetMapping("/attendance")
    public ModelAndView attendance(@AuthenticationPrincipal UserDetails userDetails) {
        var dashboard = dashboardService.getDashboard(userDetails.getUsername());
        return viewWithCommonModel("attendance", dashboard);
    }

    @GetMapping("/courses")
    public ModelAndView courses(@AuthenticationPrincipal UserDetails userDetails) {
        var dashboard = dashboardService.getDashboard(userDetails.getUsername());
        return viewWithCommonModel("courses", dashboard);
    }

    @GetMapping("/profile")
    public ModelAndView profile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(value = "updated", required = false) String updated,
                                @RequestParam(value = "profileError", required = false) String profileError) {
        var profile = dashboardService.getProfile(userDetails.getUsername());
        var modelAndView = new ModelAndView("profile");
        modelAndView.addObject("profile", profile);
        modelAndView.addObject("profileUpdateForm", new ProfileUpdateForm(
                safe(profile.username()),
                null,
                safe(profile.firstName()),
                safe(profile.lastName()),
                safe(profile.email()),
                safe(profile.profilePictureUrl())
        ));
        modelAndView.addObject("updated", updated != null);
        modelAndView.addObject("profileError", profileError != null);
        return modelAndView;
    }

    @PostMapping("/profile/update")
    public ModelAndView updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                      @Valid @ModelAttribute("profileUpdateForm") ProfileUpdateForm form,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("redirect:/profile?profileError");
        }

        userService.updateSelf(userDetails.getUsername(), new UserUpdateRequest(
                form.username(),
                form.password(),
                form.firstName(),
                form.lastName(),
                form.email(),
                form.profilePictureUrl(),
                null
        ));

        return new ModelAndView("redirect:/profile?updated");
    }

    @GetMapping("/admin/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView adminCourses(@RequestParam(value = "created", required = false) String created,
                                     @RequestParam(value = "courseError", required = false) String courseError) {
        var teachers = teacherService.getAll().stream()
                .map(t -> new TeacherOption(t.getId().toString(), t.getUser().getFirstName() + " " + t.getUser().getLastName()))
                .toList();

        var modelAndView = new ModelAndView("adminCourses");
        modelAndView.addObject("courseCreateForm", new CourseCreateForm(null, null));
        modelAndView.addObject("teachers", teachers);
        modelAndView.addObject("created", created != null);
        modelAndView.addObject("courseError", courseError != null);
        return modelAndView;
    }

    @PostMapping("/admin/courses")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView createCourseByAdmin(@Valid @ModelAttribute("courseCreateForm") CourseCreateForm form,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors() || form.teacherId() == null || form.teacherId().isBlank()) {
            return new ModelAndView("redirect:/admin/courses?courseError");
        }

        courseService.create(new CourseRequest(form.name(), java.util.UUID.fromString(form.teacherId())));
        return new ModelAndView("redirect:/admin/courses?created");
    }

    private ModelAndView viewWithCommonModel(String viewName, DashboardService.DashboardView dashboard) {
        var modelAndView = new ModelAndView(viewName);
        modelAndView.addObject("dashboard", dashboard);
        modelAndView.addObject("isStudent", ROLE_STUDENT.equals(dashboard.role().name()));
        modelAndView.addObject("isTeacher", ROLE_TEACHER.equals(dashboard.role().name()));
        modelAndView.addObject("isAdmin", ROLE_ADMIN.equals(dashboard.role().name()));
        return modelAndView;
    }

    public record TeacherOption(String id, String fullName) {
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}


