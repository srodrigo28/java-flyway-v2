#### Documentação do controller Proprietário.

```
package treinamento.treina_dev.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Importação agrupada para clareza

import jakarta.validation.Valid;
import treinamento.treina_dev.models.Proprietario;
import treinamento.treina_dev.repository.ProprietarioRepository;

/**
 * Controller REST para gerenciar as operações CRUD (Create, Read, Update, Delete) de Proprietários.
 * Esta classe é o ponto de entrada para todas as requisições HTTP relacionadas a proprietários.
 * * @RestController: Combina @Controller e @ResponseBody, indicando que os métodos
 * retornarão dados (como JSON) diretamente no corpo da resposta HTTP.
 * * @RequestMapping: Define um prefixo de rota ("path") base para todos os endpoints
 * definidos neste controller.
 */
@RestController
@RequestMapping("/proprietario")
public class ProprietarioController {

    /**
     * @Autowired: Mecanismo de injeção de dependência do Spring.
     * O Spring automaticamente cria e fornece uma instância de ProprietarioRepository
     * para que possamos interagir com o banco de dados.
     */
    @Autowired
    private ProprietarioRepository proprietarioRepository;

    /**
     * Endpoint para criar um novo proprietário.
     * Mapeado para o verbo HTTP POST em "/proprietario/inserir".
     *
     * @param proprietario O objeto Proprietario com os dados para inserção, recebido
     * no corpo (body) da requisição.
     * @Valid: Aciona as validações definidas na classe Proprietario (ex: @NotBlank, @Email).
     * @RequestBody: Converte o JSON do corpo da requisição para um objeto Java.
     * @return ResponseEntity contendo o proprietário criado com status 201 (Created)
     * ou um mapa de erro com status 409 (Conflict).
     */
    @PostMapping("/inserir")
    public ResponseEntity<?> inserir(@Valid @RequestBody Proprietario proprietario) {
        
        // --- Validações de Negócio ---
        // Antes de salvar, verificamos se os dados únicos já existem no banco.
        
        if (proprietarioRepository.existsByNome(proprietario.getNome())) {
            // Retorna HTTP 409 Conflict, um status apropriado para indicar um conflito
            // de estado (ex: recurso duplicado).
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "Já existe um proprietário com este nome."));
        }

        if (proprietarioRepository.existsByEmail(proprietario.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "Já existe um proprietário com este e-mail."));
        }

        if (proprietarioRepository.existsByTelefone(proprietario.getTelefone())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("erro", "Já existe um proprietário com este telefone."));
        }

        // Se todas as validações passarem, persistimos o novo proprietário.
        Proprietario novoProprietario = proprietarioRepository.save(proprietario);

        // Retorna HTTP 201 Created, o padrão para criação de recurso bem-sucedida.
        // O corpo da resposta contém o objeto recém-criado, incluindo seu novo ID.
        return new ResponseEntity<>(novoProprietario, HttpStatus.CREATED);
    }

    /**
     * Endpoint para listar todos os proprietários cadastrados.
     * Mapeado para o verbo HTTP GET em "/proprietario/listar".
     *
     * @return Uma lista de todos os objetos Proprietario.
     */
    @GetMapping("/listar")
    public List<Proprietario> listarTodos() {
        // O método findAll() do JpaRepository executa um "SELECT * FROM proprietario".
        return proprietarioRepository.findAll();
    }

    /**
     * Endpoint para buscar um único proprietário pelo seu ID.
     * Mapeado para o verbo HTTP GET em "/proprietario/listar/{id}".
     *
     * @param id O ID do proprietário, extraído da URL.
     * @PathVariable: Extrai o valor da variável de caminho {id} e o passa como
     * parâmetro para o método.
     * @return ResponseEntity contendo o proprietário se encontrado (status 200 OK),
     * ou uma resposta vazia com status 404 Not Found.
     */
    @GetMapping("/listar/{id}")
    public ResponseEntity<Proprietario> listarPorId(@PathVariable Long id) {
        // findById retorna um Optional, um container que pode ou não conter um valor.
        // É uma forma moderna e segura de evitar NullPointerException.
        Optional<Proprietario> proprietario = proprietarioRepository.findById(id);

        // Se o Optional contiver um valor, retorna 200 OK com o proprietário no corpo.
        // Senão, retorna 404 Not Found.
        return proprietario.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Endpoint para atualizar um proprietário existente.
     * Mapeado para o verbo HTTP PUT em "/proprietario/editar/{id}".
     *
     * @param id               O ID do proprietário a ser atualizado.
     * @param dadosAtualizados O objeto Proprietario com os novos dados.
     * @return ResponseEntity com o proprietário atualizado (200 OK), ou um erro
     * (404 Not Found ou 409 Conflict).
     */
    @PutMapping("/editar/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @Valid @RequestBody Proprietario dadosAtualizados) {

        // Passo 1: Garantir que o recurso a ser atualizado realmente existe.
        Optional<Proprietario> proprietarioExistenteOpt = proprietarioRepository.findById(id);
        if (proprietarioExistenteOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Passo 2: Validar a unicidade dos dados, evitando conflitos com OUTROS registros.
        // A lógica de conflito: verifica se o novo nome já existe (isPresent) E se o ID do
        // proprietário encontrado é DIFERENTE do ID que estamos editando.
        // Isso permite que um usuário salve seus dados sem alterar o próprio nome.
        Optional<Proprietario> conflitoNome = proprietarioRepository.findByNome(dadosAtualizados.getNome());
        if (conflitoNome.isPresent() && !conflitoNome.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("erro", "Nome já está em uso por outro proprietário."));
        }

        Optional<Proprietario> conflitoEmail = proprietarioRepository.findByEmail(dadosAtualizados.getEmail());
        if (conflitoEmail.isPresent() && !conflitoEmail.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("erro", "E-mail já está em uso por outro proprietário."));
        }

        Optional<Proprietario> conflitoTelefone = proprietarioRepository.findByTelefone(dadosAtualizados.getTelefone());
        if (conflitoTelefone.isPresent() && !conflitoTelefone.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("erro", "Telefone já está em uso por outro proprietário."));
        }
        
        // Passo 3: Atualizar os dados do objeto que foi recuperado do banco.
        // Esta é a maneira correta de fazer um update com JPA/Hibernate.
        Proprietario proprietarioParaAtualizar = proprietarioExistenteOpt.get();
        proprietarioParaAtualizar.setNome(dadosAtualizados.getNome());
        proprietarioParaAtualizar.setEmail(dadosAtualizados.getEmail());
        proprietarioParaAtualizar.setTelefone(dadosAtualizados.getTelefone());
        
        // Passo 4: Salvar a entidade atualizada. O JPA entende que é um UPDATE, não um INSERT.
        Proprietario proprietarioAtualizado = proprietarioRepository.save(proprietarioParaAtualizar);

        // Passo 5: Retornar 200 OK com o objeto atualizado.
        return ResponseEntity.ok(proprietarioAtualizado);
    }


    /**
     * Endpoint para deletar um proprietário pelo seu ID.
     * Mapeado para o verbo HTTP DELETE em "/proprietario/deletar/{id}".
     *
     * @param id O ID do proprietário a ser deletado.
     * @return Uma resposta vazia com status 204 No Content (sucesso) ou 404 Not Found (falha).
     */
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        
        // Antes de deletar, é uma boa prática verificar se o recurso existe.
        // O método existsById é otimizado para essa checagem.
        if (!proprietarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Se o recurso existe, o comando de exclusão é executado.
        proprietarioRepository.deleteById(id);
        
        // Retorna HTTP 204 No Content: indica que a operação foi bem-sucedida,
        // mas não há conteúdo para retornar no corpo da resposta. É o padrão para DELETE.
        return ResponseEntity.noContent().build();
    }
}
```