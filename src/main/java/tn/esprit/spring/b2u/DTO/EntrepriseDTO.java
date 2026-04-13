package tn.esprit.spring.b2u.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EntrepriseDTO {

    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private String sector;
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{8}$", message = "Phone must contain exactly 8 digits")
    private String phone;
}
