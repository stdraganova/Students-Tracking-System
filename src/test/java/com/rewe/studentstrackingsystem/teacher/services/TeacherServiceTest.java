package com.rewe.studentstrackingsystem.teacher.services;

import com.rewe.studentstrackingsystem.exception.ResourceNotFoundException;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherRequest;
import com.rewe.studentstrackingsystem.teacher.dtos.TeacherResponse;
import com.rewe.studentstrackingsystem.teacher.entity.Teacher;
import com.rewe.studentstrackingsystem.teacher.mapper.TeacherMapper;
import com.rewe.studentstrackingsystem.teacher.repository.TeacherRepository;
import com.rewe.studentstrackingsystem.user.entity.User;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

	@Mock
	private TeacherRepository teacherRepository;

	@Mock
	private TeacherMapper mapper;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private TeacherService teacherService;

	private TeacherRequest request;

    @BeforeEach
	void setUp() {
		request = new TeacherRequest(UUID.randomUUID());
        Teacher teacher = new Teacher();
		teacher.setId(UUID.randomUUID());
	}

	@Test
	void saveShouldReturnMappedResponse() {
		TeacherResponse response = null;
		User user = new User();
		user.setId(request.userId());
		final Teacher[] savedTeacher = new Teacher[1];

		when(userRepository.findById(request.userId())).thenReturn(Optional.of(user));
		when(teacherRepository.save(any(Teacher.class))).thenAnswer(invocation -> {
			savedTeacher[0] = invocation.getArgument(0);
			return savedTeacher[0];
		});
		when(mapper.toResponse(any(Teacher.class))).thenReturn(response);

		TeacherResponse result = teacherService.save(request);

		assertNull(result);
		assertNotNull(savedTeacher[0]);
		assertEquals(user, savedTeacher[0].getUser());
		assertEquals(savedTeacher[0], user.getTeacher());
		verify(userRepository).findById(request.userId());
		verify(teacherRepository).save(any(Teacher.class));
		verify(mapper).toResponse(savedTeacher[0]);
	}

	@Test
	void deleteShouldRemoveTeacherWhenItExists() {
		UUID id = UUID.randomUUID();
		when(teacherRepository.existsById(id)).thenReturn(true);

		teacherService.delete(id);

		verify(teacherRepository).deleteById(id);
	}

	@Test
	void deleteShouldThrowWhenTeacherMissing() {
		UUID id = UUID.randomUUID();
		when(teacherRepository.existsById(id)).thenReturn(false);

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
				() -> teacherService.delete(id));

		assertEquals("Teacher not found with id: " + id, ex.getMessage());
		verify(teacherRepository, never()).deleteById(any());
	}
}

