# Achados e Perdidos

Sistema web em desenvolvimento para auxiliar a comunidade da Universidade Federal do Espírito Santo (UFES) na divulgação e recuperação de objetos perdidos ou encontrados no campus.

## Descrição do Sistema

### Problema

Objetos pessoais são frequentemente perdidos ou encontrados em salas, laboratórios, bibliotecas, corredores e demais espaços da universidade. Sem um canal centralizado, a comunicação costuma acontecer de forma dispersa, por grupos de mensagens, redes sociais ou avisos informais. Isso dificulta o encontro entre quem perdeu um objeto e quem o encontrou.

O sistema de Achados e Perdidos busca centralizar essas informações em uma aplicação web. Por meio dela, a comunidade universitária poderá publicar anúncios, consultar objetos registrados e acompanhar se uma ocorrência ainda está ativa ou já foi resolvida.

### Usuários

- **Usuário comum:** integrante da comunidade universitária, como estudante, professor, servidor ou colaborador. Poderá consultar anúncios e, após autenticação, publicar e gerenciar os próprios anúncios.
- **Administrador:** responsável por acompanhar o conteúdo publicado e realizar ações administrativas quando necessário.

### Principais Funcionalidades

O escopo previsto para o sistema inclui:

- Cadastro e autenticação de usuários;
- Publicação de anúncios de objetos perdidos ou encontrados;
- Listagem e visualização dos detalhes dos anúncios;
- Busca e filtros por tipo, categoria e local;
- Edição e exclusão dos próprios anúncios;
- Inclusão de imagem do objeto;
- Marcação de anúncios como resolvidos quando o objeto for devolvido;
- Administração dos anúncios publicados.

As seções de funcionalidades implementadas e planejadas, apresentadas mais adiante, indicam o estado atual do desenvolvimento.

## Diagrama de Classes do Domínio

```mermaid
classDiagram
    class Usuario {
        Long id
        String nome
        String email
        String senha
        TipoUsuario tipoUsuario
    }

    class Anuncio {
        Long id
        String titulo
        String descricao
        TipoAnuncio tipoAnuncio
        String local
        LocalDate data
        String imagem
        StatusAnuncio status
    }

    class Categoria {
        Long id
        String nome
    }

    class TipoUsuario {
        <<enum>>
        USUARIO
        ADMIN
    }

    class TipoAnuncio {
        <<enum>>
        PERDIDO
        ENCONTRADO
    }

    class StatusAnuncio {
        <<enum>>
        ATIVO
        RESOLVIDO
    }

    Usuario "1" --> "0..*" Anuncio
    Categoria "1" --> "0..*" Anuncio
    Usuario --> TipoUsuario
    Anuncio --> TipoAnuncio
    Anuncio --> StatusAnuncio
```

## Ferramentas Escolhidas

- **Git**: controle de versão.
- **GitHub Issues**: acompanhamento das tarefas e funcionalidades implementadas.
- **Maven**: build e gerenciamento de dependências.
- **H2 Database**: banco em memória para desenvolvimento.
- **JavaDoc**: documentação do código Java.
- **Markdown**: documentação no README/Wiki.


## Frameworks Reutilizados

- **Spring Security**: autenticacao, autorizacao, login/logout e protecao CSRF.
- **Spring Boot**: base da aplicação.
- **Spring Web**: criação dos controllers e rotas web.
- **Spring Data JPA**: persistência de dados.
- **Hibernate**: implementação JPA.
- **Thymeleaf**: páginas HTML dinâmicas.
- **H2 Database**: banco de dados em memória. (inicialmente para testes)

## Como Executar

Requisitos:

- Java 17
- Maven

Comandos principais:

```bash
mvn spring-boot:run
```

A aplicacao fica disponivel em `http://localhost:8080`.

Para executar os testes automatizados:

```bash
mvn test
```

Para gerar o arquivo `.jar`:

```bash
mvn package
```


## Funcionalidades Implementadas

- Cadastro, login e logout de usuarios
- Listagem inicial de anúncios ativos
- Cadastro inicial de anúncios via formulário
- Visualização dos detalhes dos anúncios
- Busca e filtros por texto, tipo, categoria e local
- Página "Meus anúncios" com listagem das publicações do usuário autenticado
- Edição de anúncios
- Exclusão de anúncios
- Marcação de anúncios como resolvidos
- Histórico/listagem de anúncios resolvidos
- Permissao para editar, resolver e excluir apenas anuncios do proprio usuario ou por administrador
- Testes automatizados de fluxo, cenarios especificos e teste unitario de armazenamento de imagem
- Upload de imagem nos anúncios
- Remocao de imagem ao editar anuncios
- Entidades principais do domínio
- Banco H2 em memória
- Página inicial com Thymeleaf
- Dados iniciais de demonstracao com administrador, usuario comum e anuncios de exemplo

## Testes Automatizados

- Executados com `mvn test`.
- Cobrem fluxos web com Spring Boot, MockMvc, H2 e JdbcTemplate.
- Incluem cenarios especificos de upload invalido, filtros, e-mail duplicado, senhas diferentes, pagina "Meus anuncios", permissoes de edicao/resolucao/exclusao e remocao fisica de imagem.
- Incluem testes unitarios pequenos de `ImagemStorageService`, sem subir servidor, Spring ou banco.
- Resultado atual: 29 testes, 0 falhas.

## Acesso de Demonstracao

- **Administrador:** secretaria@faculdade.edu
- **Senha:** 123456
- **Usuario comum:** aluno@faculdade.edu
- **Senha:** 123456

O usuario comum e criado com anuncios de exemplo sem imagem para facilitar a demonstracao da pagina "Meus anuncios".

## Funcionalidades Planejadas

- Melhorias na validacao de formularios e mensagens de erro

## Ferramentas Planejadas

- GitHub Actions para CI/CD
- Docker para containerização futura
