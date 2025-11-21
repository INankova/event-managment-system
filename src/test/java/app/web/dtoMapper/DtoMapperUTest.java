package app.web.dtoMapper;

import com.example.event_management_system.User.model.User;
import com.example.event_management_system.web.dto.UserEditRequest;
import com.example.event_management_system.web.mapper.DtoMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DtoMapperUTest {

    @Test
    void mapUserToUserEditRequest_shouldMapAllFieldsCorrectly() {

        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePictureUrl("pic.png")
                .phoneNumber("0888123456")
                .build();

        UserEditRequest result = DtoMapper.mapUserToUserEditRequest(user);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("pic.png", result.getProfilePicture());
        assertEquals("0888123456", result.getPhoneNumber());

        assertNull(result.getNewPassword());
        assertNull(result.getConfirmNewPassword());
        assertNull(result.getCurrentPassword());
    }
}


