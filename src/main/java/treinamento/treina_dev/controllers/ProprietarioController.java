package treinamento.treina_dev.controllers;

import java.util.List;
import java.util.Optional;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import treinamento.treina_dev.models.Proprietario;
import treinamento.treina_dev.repository.ProprietarioRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/proprietario") // <-- ROTA PAI ADICIONADA AQUI
public class ProprietarioController {

    @Autowired
    private ProprietarioRepository proprietarioRepository;

    @PostMapping("inserir") // inserir um novo proprietário
    public ResponseEntity<?> inserir(@Valid @RequestBody Proprietario proprietario) {
        // 1. Validação de nome duplicado
        if (proprietarioRepository.existsByNome(proprietario.getNome())) {
            // Retorna HTTP 409 (Conflict) se o nome já existir
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "Já existe um proprietário com este nome."));
        }
        // 2. Validação de email duplicado
        if (proprietarioRepository.existsByEmail(proprietario.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "Já existe um proprietário com este e-mail."));
        }
        // 3. Validação de telefone duplicado
        if (proprietarioRepository.existsByTelefone(proprietario.getTelefone())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "Já existe um proprietário com este telefone."));
        }

        // 4. Se tudo estiver ok, salva no banco
        Proprietario novoProprietario = proprietarioRepository.save(proprietario);

        return new ResponseEntity<>(novoProprietario, HttpStatus.CREATED);
    }

    @GetMapping("listar") // listar todos os proprietários
    public List<Proprietario> listarTodos() {
        return proprietarioRepository.findAll();
    }

    @GetMapping("listar/{id}") // listar um proprietário específico
    public ResponseEntity<Proprietario> listarPorId(@PathVariable Long id) {
        Optional<Proprietario> proprietario = proprietarioRepository.findById(id);
        if (proprietario.isPresent()) {
            return ResponseEntity.ok(proprietario.get());
        }
        return ResponseEntity.notFound().build(); // Retorna 404 Not Found se não encontrar
    }

    @DeleteMapping("/deletar/{id}") // deletar um proprietário específico
    public ResponseEntity<?> deletar(@PathVariable Long id) {

        // 1. Verifica se o proprietário com o ID fornecido existe no banco.
        boolean existe = proprietarioRepository.existsById(id);

        if (!existe) {
            // 2. Se não existir, retorna o status HTTP 404 (Not Found).
            return ResponseEntity.notFound().build();
        }

        // 3. Se existir, deleta o proprietário pelo ID.
        proprietarioRepository.deleteById(id);

        // 4. Retorna o status HTTP 204 (No Content), que é o padrão para
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/editar/{id}") // editar um proprietário específico
    public ResponseEntity<?> editar(@PathVariable Long id, @Valid @RequestBody Proprietario dadosAtualizados) {

        // 1. Validação de Existência: Verifica se o ID existe no banco
        Optional<Proprietario> proprietarioExistenteOpt = proprietarioRepository.findById(id);
        if (proprietarioExistenteOpt.isEmpty()) {
            return ResponseEntity.notFound().build(); // Retorna 404 Not Found
        }

        // 2. Validação de Unicidade: Verifica se os dados únicos já pertencem a OUTRO
        // usuário
        Optional<Proprietario> conflitoNome = proprietarioRepository.findByNome(dadosAtualizados.getNome());
        if (conflitoNome.isPresent() && !conflitoNome.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "Nome já está em uso por outro proprietário."));
        }

        Optional<Proprietario> conflitoEmail = proprietarioRepository.findByEmail(dadosAtualizados.getEmail());
        if (conflitoEmail.isPresent() && !conflitoEmail.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "E-mail já está em uso por outro proprietário."));
        }

        Optional<Proprietario> conflitoTelefone = proprietarioRepository.findByTelefone(dadosAtualizados.getTelefone());
        if (conflitoTelefone.isPresent() && !conflitoTelefone.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "Telefone já está em uso por outro proprietário."));
        }

        // 3. Se todas as validações passaram, atualiza o objeto existente
        Proprietario proprietarioParaAtualizar = proprietarioExistenteOpt.get();
        proprietarioParaAtualizar.setNome(dadosAtualizados.getNome());
        proprietarioParaAtualizar.setEmail(dadosAtualizados.getEmail());
        proprietarioParaAtualizar.setTelefone(dadosAtualizados.getTelefone());

        // 4. Salva as alterações no banco
        Proprietario proprietarioAtualizado = proprietarioRepository.save(proprietarioParaAtualizar);

        // 5. Retorna 200 OK com o objeto atualizado no corpo da resposta
        return ResponseEntity.ok(proprietarioAtualizado);
    }
}