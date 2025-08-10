package treinamento.treina_dev.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "proprietario")
public class Proprietario {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "O nome não pode estar em branco.")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String nome;
    
    @NotBlank(message = "O e-mail не может быть пустым.")
    @Email(message = "O formato do e-mail é inválido.")
    private String email;

    @NotBlank(message = "O telefone não pode estar em branco.")
    @Pattern(regexp = "^\\d{10,11}$", 
    message = "O telefone deve conter 10 ou 11 dígitos, sem formatação.")
    private String telefone;
}