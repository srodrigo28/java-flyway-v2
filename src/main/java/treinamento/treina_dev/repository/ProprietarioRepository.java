package treinamento.treina_dev.repository;

import java.util.Optional;
import treinamento.treina_dev.models.Proprietario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProprietarioRepository extends JpaRepository<Proprietario, Long> {

    // Método para verificar se um proprietário com o mesmo nome já existe
    boolean existsByNome(String nome);
    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);

    // Métodos novos para a validação do 'editar'
    Optional<Proprietario> findByNome(String nome);
    Optional<Proprietario> findByEmail(String email);
    Optional<Proprietario> findByTelefone(String telefone);
}