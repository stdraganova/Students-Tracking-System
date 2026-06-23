package com.rewe.studentstrackingsystem.web.controller;

import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.teacher.services.TeacherWorkbenchService;
import com.rewe.studentstrackingsystem.web.model.TeacherAttendanceForm;
import com.rewe.studentstrackingsystem.web.model.TeacherGradeForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherCoursePageController {

    private static final String COURSES_REDIRECT = "redirect:/courses";

    private final TeacherWorkbenchService teacherWorkbenchService;

    @GetMapping("/teacher/courses/{courseId}/students")
    public ModelAndView courseStudents(@PathVariable("courseId") UUID courseId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam(value = "success", required = false) String success,
                                       @RequestParam(value = "error", required = false) String error) {
        TeacherWorkbenchService.TeacherCourseStudentsView view;
        try {
            view = teacherWorkbenchService.getCourseStudentsView(userDetails.getUsername(), courseId);
        } catch (Exception _) {
            return new ModelAndView(COURSES_REDIRECT);
        }

        var mv = new ModelAndView("teacher-course-students");
        mv.addObject("courseId", courseId.toString());
        mv.addObject("courseName", view.courseName());
        mv.addObject("students", view.students());
        mv.addObject("success", success);
        mv.addObject("error", error);
        return mv;
    }

    @GetMapping("/teacher/courses/{courseId}/students/{studentId}")
    public ModelAndView studentDetail(@PathVariable("courseId") UUID courseId,
                                      @PathVariable("studentId") UUID studentId,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      @RequestParam(value = "success", required = false) String success,
                                      @RequestParam(value = "error", required = false) String error) {
        TeacherWorkbenchService.TeacherStudentDetailView view;
        try {
            view = teacherWorkbenchService.getStudentDetailView(userDetails.getUsername(), courseId, studentId);
        } catch (Exception _) {
            return new ModelAndView("redirect:/teacher/courses/" + courseId + "/students");
        }

        var mv = new ModelAndView("teacher-student-detail");
        mv.addObject("courseId", courseId.toString());
        mv.addObject("courseName", view.courseName());
        mv.addObject("studentId", studentId.toString());
        mv.addObject("studentName", view.studentName());
        mv.addObject("grades", view.grades());
        mv.addObject("attendances", view.attendances());
        mv.addObject("success", success);
        mv.addObject("error", error);
        return mv;
    }

    @PostMapping("/teacher/courses/{courseId}/students/{studentId}/grades")
    public ModelAndView addGrade(@PathVariable("courseId") UUID courseId,
                                 @PathVariable("studentId") UUID studentId,
                                 @Valid @ModelAttribute TeacherGradeForm form,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            return redirectToStudent(courseId, studentId, null, "Please provide a valid grade value and date");
        }
        try {
            teacherWorkbenchService.addGrade(userDetails.getUsername(),
                    new GradeRequest(form.grade(), form.creationDate(), studentId, courseId));
            return redirectToStudent(courseId, studentId, "Grade added", null);
        } catch (ValidationException ex) {
            return redirectToStudent(courseId, studentId, null, ex.getMessage());
        } catch (Exception _) {
            return redirectToStudent(courseId, studentId, null, "Could not add grade");
        }
    }

    @PostMapping("/teacher/courses/{courseId}/students/{studentId}/grades/{gradeId}/update")
    public ModelAndView updateGrade(@PathVariable("courseId") UUID courseId,
                                    @PathVariable("studentId") UUID studentId,
                                    @PathVariable("gradeId") UUID gradeId,
                                    @Valid @ModelAttribute TeacherGradeForm form,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            return redirectToStudent(courseId, studentId, null, "Please provide a valid grade value and date");
        }
        try {
            teacherWorkbenchService.updateGrade(userDetails.getUsername(), gradeId,
                    new GradeRequest(form.grade(), form.creationDate(), studentId, courseId));
            return redirectToStudent(courseId, studentId, "Grade updated", null);
        } catch (ValidationException ex) {
            return redirectToStudent(courseId, studentId, null, ex.getMessage());
        } catch (Exception _) {
            return redirectToStudent(courseId, studentId, null, "Could not update grade");
        }
    }

    @PostMapping("/teacher/courses/{courseId}/students/{studentId}/grades/{gradeId}/delete")
    public ModelAndView deleteGrade(@PathVariable("courseId") UUID courseId,
                                    @PathVariable("studentId") UUID studentId,
                                    @PathVariable("gradeId") UUID gradeId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            teacherWorkbenchService.deleteGrade(userDetails.getUsername(), courseId, studentId, gradeId);
            return redirectToStudent(courseId, studentId, "Grade removed", null);
        } catch (ValidationException ex) {
            return redirectToStudent(courseId, studentId, null, ex.getMessage());
        } catch (Exception _) {
            return redirectToStudent(courseId, studentId, null, "Could not remove grade");
        }
    }

    @PostMapping("/teacher/courses/{courseId}/students/{studentId}/attendance")
    public ModelAndView addAttendance(@PathVariable("courseId") UUID courseId,
                                      @PathVariable("studentId") UUID studentId,
                                      @Valid @ModelAttribute TeacherAttendanceForm form,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            return redirectToStudent(courseId, studentId, null, "Please provide a valid attendance date and status");
        }
        try {
            teacherWorkbenchService.addAttendance(
                    userDetails.getUsername(),
                    courseId,
                    studentId,
                    form.attendanceDate(),
                    Boolean.TRUE.equals(form.present())
            );
            return redirectToStudent(courseId, studentId, "Attendance added", null);
        } catch (ValidationException ex) {
            return redirectToStudent(courseId, studentId, null, ex.getMessage());
        } catch (Exception _) {
            return redirectToStudent(courseId, studentId, null, "Could not add attendance");
        }
    }

    @PostMapping("/teacher/courses/{courseId}/students/{studentId}/attendance/{attendanceId}/update")
    public ModelAndView updateAttendance(@PathVariable("courseId") UUID courseId,
                                         @PathVariable("studentId") UUID studentId,
                                         @PathVariable("attendanceId") UUID attendanceId,
                                         @Valid @ModelAttribute TeacherAttendanceForm form,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (bindingResult.hasErrors()) {
            return redirectToStudent(courseId, studentId, null, "Please provide a valid attendance date and status");
        }
        try {
            teacherWorkbenchService.updateAttendance(
                    userDetails.getUsername(),
                    attendanceId,
                    courseId,
                    studentId,
                    form.attendanceDate(),
                    Boolean.TRUE.equals(form.present())
            );
            return redirectToStudent(courseId, studentId, "Attendance updated", null);
        } catch (ValidationException ex) {
            return redirectToStudent(courseId, studentId, null, ex.getMessage());
        } catch (Exception _) {
            return redirectToStudent(courseId, studentId, null, "Could not update attendance");
        }
    }

    @PostMapping("/teacher/courses/{courseId}/students/{studentId}/attendance/{attendanceId}/delete")
    public ModelAndView deleteAttendance(@PathVariable("courseId") UUID courseId,
                                         @PathVariable("studentId") UUID studentId,
                                         @PathVariable("attendanceId") UUID attendanceId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        try {
            teacherWorkbenchService.deleteAttendance(userDetails.getUsername(), courseId, studentId, attendanceId);
            return redirectToStudent(courseId, studentId, "Attendance removed", null);
        } catch (ValidationException ex) {
            return redirectToStudent(courseId, studentId, null, ex.getMessage());
        } catch (Exception _) {
            return redirectToStudent(courseId, studentId, null, "Could not remove attendance");
        }
    }

    private ModelAndView redirectToStudent(UUID courseId, UUID studentId, String success, String error) {
        String base = "/teacher/courses/" + courseId + "/students/" + studentId;
        if (success != null) return new ModelAndView("redirect:" + base + "?success=" + encode(success));
        return new ModelAndView("redirect:" + base + "?error=" + encode(error != null ? error : "Unexpected error"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
