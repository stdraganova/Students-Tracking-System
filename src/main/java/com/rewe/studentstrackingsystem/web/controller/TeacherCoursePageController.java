package com.rewe.studentstrackingsystem.web.controller;

import com.rewe.studentstrackingsystem.attendance.dtos.AttendanceRequest;
import com.rewe.studentstrackingsystem.attendance.repository.AttendanceRepository;
import com.rewe.studentstrackingsystem.attendance.services.AttendanceService;
import com.rewe.studentstrackingsystem.course.repository.CourseRepository;
import com.rewe.studentstrackingsystem.exception.ValidationException;
import com.rewe.studentstrackingsystem.grade.dto.GradeRequest;
import com.rewe.studentstrackingsystem.grade.repository.GradeRepository;
import com.rewe.studentstrackingsystem.grade.services.GradeService;
import com.rewe.studentstrackingsystem.student.repository.StudentRepository;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
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
import java.util.Comparator;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherCoursePageController {

    private static final String COURSES_REDIRECT = "redirect:/courses";

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeService gradeService;
    private final AttendanceService attendanceService;
    private final TeacherWorkbenchService teacherWorkbenchService;

    @GetMapping("/teacher/courses/{courseId}/students")
    public ModelAndView courseStudents(@PathVariable("courseId") UUID courseId,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam(value = "success", required = false) String success,
                                       @RequestParam(value = "error", required = false) String error) {
        var teacher = teacherRepository.findByUserUsername(userDetails.getUsername()).orElse(null);
        if (teacher == null) return new ModelAndView(COURSES_REDIRECT);

        var course = courseRepository.findByIdAndTeacherId(courseId, teacher.getId()).orElse(null);
        if (course == null) return new ModelAndView(COURSES_REDIRECT);

        var students = studentRepository.findDistinctByCoursesId(courseId).stream()
                .map(s -> new StudentListRow(
                        s.getId().toString(),
                        s.getUser().getFirstName() + " " + s.getUser().getLastName(),
                        s.getUser().getEmail()))
                .sorted(Comparator.comparing(StudentListRow::fullName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        var mv = new ModelAndView("teacher-course-students");
        mv.addObject("courseId", course.getId().toString());
        mv.addObject("courseName", course.getName());
        mv.addObject("students", students);
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
        var teacher = teacherRepository.findByUserUsername(userDetails.getUsername()).orElse(null);
        if (teacher == null) return new ModelAndView(COURSES_REDIRECT);

        var course = courseRepository.findByIdAndTeacherId(courseId, teacher.getId()).orElse(null);
        if (course == null) return new ModelAndView(COURSES_REDIRECT);

        var student = studentRepository.findById(studentId).orElse(null);
        if (student == null)
            return new ModelAndView("redirect:/teacher/courses/" + courseId + "/students");

        var grades = gradeRepository.findByCourseIdOrderByCreationDateDesc(courseId).stream()
                .filter(g -> g.getStudent().getId().equals(studentId))
                .map(g -> new GradeRow(g.getId().toString(), g.getGrade(), g.getCreationDate()))
                .toList();

        var attendances = attendanceRepository
                .findByCourseIdAndTeacherIdOrderByAttendanceDateDesc(courseId, teacher.getId()).stream()
                .filter(a -> a.getStudent().getId().equals(studentId))
                .map(a -> new AttendanceRow(a.getId().toString(), a.getAttendanceDate(), a.isPresent()))
                .toList();

        var mv = new ModelAndView("teacher-student-detail");
        mv.addObject("courseId", courseId.toString());
        mv.addObject("courseName", course.getName());
        mv.addObject("studentId", studentId.toString());
        mv.addObject("studentName",
                student.getUser().getFirstName() + " " + student.getUser().getLastName());
        mv.addObject("grades", grades);
        mv.addObject("attendances", attendances);
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
        var teacher = teacherRepository.findByUserUsername(userDetails.getUsername()).orElse(null);
        if (teacher == null)
            return redirectToStudent(courseId, studentId, null, "Teacher profile not found");

        var grade = gradeRepository.findByIdAndCourseIdAndCourseTeacherId(gradeId, courseId, teacher.getId())
                .orElse(null);
        if (grade == null || !grade.getStudent().getId().equals(studentId))
            return redirectToStudent(courseId, studentId, null, "Grade not found");

        try {
            gradeService.delete(gradeId);
            return redirectToStudent(courseId, studentId, "Grade removed", null);
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
        var teacher = teacherRepository.findByUserUsername(userDetails.getUsername()).orElse(null);
        if (teacher == null) return new ModelAndView(COURSES_REDIRECT);

        if (bindingResult.hasErrors()) {
            return redirectToStudent(courseId, studentId, null, "Please provide a valid attendance date and status");
        }
        try {
            teacherWorkbenchService.addAttendance(userDetails.getUsername(),
                    new AttendanceRequest(form.attendanceDate(),
                            Boolean.TRUE.equals(form.present()), studentId, teacher.getId(), courseId));
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
        var teacher = teacherRepository.findByUserUsername(userDetails.getUsername()).orElse(null);
        if (teacher == null) return new ModelAndView(COURSES_REDIRECT);

        if (bindingResult.hasErrors()) {
            return redirectToStudent(courseId, studentId, null, "Please provide a valid attendance date and status");
        }
        try {
            teacherWorkbenchService.updateAttendance(userDetails.getUsername(), attendanceId,
                    new AttendanceRequest(form.attendanceDate(),
                            Boolean.TRUE.equals(form.present()), studentId, teacher.getId(), courseId));
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
        var teacher = teacherRepository.findByUserUsername(userDetails.getUsername()).orElse(null);
        if (teacher == null)
            return redirectToStudent(courseId, studentId, null, "Teacher profile not found");

        var attendance = attendanceRepository
                .findByIdAndCourseIdAndTeacherId(attendanceId, courseId, teacher.getId()).orElse(null);
        if (attendance == null || !attendance.getStudent().getId().equals(studentId))
            return redirectToStudent(courseId, studentId, null, "Attendance record not found");

        try {
            attendanceService.delete(attendanceId);
            return redirectToStudent(courseId, studentId, "Attendance removed", null);
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

    public record StudentListRow(String studentId, String fullName, String email) {
    }

    public record GradeRow(String id, Double value, java.time.LocalDate date) {
    }

    public record AttendanceRow(String id, java.time.LocalDate date, boolean present) {
    }
}
