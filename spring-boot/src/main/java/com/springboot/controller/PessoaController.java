package com.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.springboot.model.Pessoa;
import com.springboot.model.Telefone;
import com.springboot.repository.PessoaRepository;
import com.springboot.repository.ProfissaoRepository;
import com.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ReportUtil reportUtil;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;

	
	
	/*
	 * Chama pagina de CADASTRO DE PESSOA
	 * */
	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");	
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());
		
		return andView;
	}
	
	
	
	/*
	 * M??todo de PAGINA????O
	 * */
	@GetMapping("/pessoaspag")
	public ModelAndView carregaPessoaPorPaginacao(@PageableDefault(size = 5) Pageable pageable, ModelAndView model,
			@RequestParam("nomepesquisa") String nomepesquisa) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageable);
		model.addObject("pessoas", pagePessoa);
		model.addObject("pessoaobj", new Pessoa());
		model.addObject("nomepesquisa", nomepesquisa);
		
		model.setViewName("cadastro/cadastropessoa");
		
		return model;
	}
	
	
	
	/*
	 * PESQUISAR PESSOA POR NOME
	 * */
	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa, @RequestParam("sexoPesquisa") String sexoPesquisa, 
			@PageableDefault(size = 5, sort = {"nome"}) Pageable pageble) {
		
		
		Page<Pessoa> pessoas = null;
		
		if(sexoPesquisa != "" && !sexoPesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexoNomePage(nomepesquisa, sexoPesquisa, pageble);
		} else {
			pessoas = pessoaRepository.findPessoaByNamePage(nomepesquisa, pageble);
		}
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoas);
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("nomepesquisa", nomepesquisa);
		return andView;
	}

	
	/*
	 * Imprime RELATORIO PDF
	 * */
	@GetMapping("**/pesquisarpessoa")
	public void imprimePdf(@RequestParam("nomepesquisa") String nomepesquisa, 
			@RequestParam("sexoPesquisa") String sexoPesquisa, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		List<Pessoa> pessoas = new ArrayList<Pessoa>();
		
		if(sexoPesquisa != null && !sexoPesquisa.isEmpty() &&
				nomepesquisa != null && !nomepesquisa.isEmpty()) {
			
			pessoas = pessoaRepository.findPessoaByNameSexo(nomepesquisa, sexoPesquisa);
			
		} else if(nomepesquisa != null && !nomepesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaByName(nomepesquisa);
		}else if(sexoPesquisa != null && !sexoPesquisa.isEmpty()) {
			pessoas = pessoaRepository.findPessoaBySexo(sexoPesquisa);
		} else {
			pessoas = (List<Pessoa>) pessoaRepository.findAll();
		}
		
		
		//Chamar o servi??o que faz a gera????o do relat??rio
		byte[] pdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());
		
		//Tamanho da resposta
		response.setContentLengthLong(pdf.length);
		
		//Definir na resposta o tipo de arquivo
		response.setContentType("application/octet-stream");
		
		//Cabe??alho da resposta
		String headKey = "Content-Disposition";
		String headValue = String.format("attachment; filename=\"%s\"", "relatorio.pdf");
		response.setHeader(headKey, headValue);
		
		
		//Finaliza a resposta para o navegador
		response.getOutputStream().write(pdf);
		
		
	}
	
	
	/*
	 * Download Curriculo
	 * */
	@GetMapping("**/baixarcurriculo/{idpessoa}")
	public void baixarCurriculo(@PathVariable("idpessoa") Long idpessoa, HttpServletResponse response) throws IOException {
		
		//Consultar objeto pessoa no banco de dados
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		
		if(pessoa.getCurriculo() != null) {
			
			//Setar tamanho da resposta
			response.setContentLength(pessoa.getCurriculo().length);
			
			//Tipo do arquivo para download ou pode ser generica: application/octet-stream
			response.setContentType(pessoa.getTipoArquivo());
			
			//Define o cabecalho da resposta
			String headerKey = "content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeArquivo());
			response.setHeader(headerKey, headerValue);
			
			//finaliza a resposta passando o arquivo
			response.getOutputStream().write(pessoa.getCurriculo());			
			
		}
		
	}
	

	/*
	 * SALVAR
	 * */
	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa", consumes = {"multipart/form-data"}) //** -> ignora tudo que vem antes
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {

		
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		/*
		 * Valida erros do formul??rio
		 * Model Pessoa
		 * */
		if(bindingResult.hasErrors()) {
			
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
			
			modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
			modelAndView.addObject("pessoaobj", pessoa); //Mantem formul??rio preenchido
			modelAndView.addObject("profissoes", profissaoRepository.findAll());
			
			List<String> msg = new ArrayList<String>();
			
			for(ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage()); //Vem das @anota????es
			}
			
			modelAndView.addObject("msg", msg);
			
			return modelAndView;
		}
		
		
		
		//Cadastrando curriculo
		if(file.getSize() > 0) {
			pessoa.setCurriculo(file.getBytes());
			pessoa.setTipoArquivo(file.getContentType());
			pessoa.setNomeArquivo(file.getOriginalFilename());
		} else {
			
			//Caso esteja editando
			if(pessoa.getId() != null && pessoa.getId() > 0) {
				Pessoa pessoaTempo = pessoaRepository.findById(pessoa.getId()).get();
				pessoa.setCurriculo(pessoaTempo.getCurriculo());
				pessoa.setTipoArquivo(pessoaTempo.getTipoArquivo());
				pessoa.setNomeArquivo(pessoaTempo.getNomeArquivo());
			}
		}
		
		
		
		pessoaRepository.save(pessoa);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());
		
		return andView;

	}


	/*
	 * EDITAR
	 * */
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", pessoa.get());
		andView.addObject("profissoes", profissaoRepository.findAll());

		return andView;
	}
	
	
	/*
	 * ADICIONA TELEFONE a PESSOA
	 * */
	@PostMapping("**/addfonepessoa/{idpessoa}")
	public ModelAndView addFonePessoa(Telefone telefone, @PathVariable("idpessoa") Long idpessoa) {
							
		boolean valid = true;
		List<String> msg = new ArrayList<String>();		
		Pessoa pessoa = pessoaRepository.findById(idpessoa).get();
		ModelAndView andView = new ModelAndView("cadastro/telefones");
		
		//N??mero
		if(telefone != null && 
				(telefone.getNumero() != null && telefone.getNumero().isEmpty()) || 
				telefone.getNumero() == null) {			
			valid = false;
			msg.add("N??mero deve ser informado");			
		}
		
		//Tipo
		if(telefone != null && 
				(telefone.getTipo() != null && telefone.getTipo().isEmpty()) || 
				telefone.getTipo() == null) {
			valid = false;			
			msg.add("Tipo deve ser informado");			
		}
		
		
		//Salva Telefone
		if(valid) {
			telefone.setPessoa(pessoa);		
			telefoneRepository.save(telefone);
		} else {
			andView.addObject("msg", msg);
		} 
		
		
		andView.addObject("pessoaobj", pessoa);
		andView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
		return andView;
		
	}
	
	
	/*
	 * Chama p??gina TELEFONES relacionado com PESSOA
	 * */
	@GetMapping("/telefones/{idpessoa}")
	public ModelAndView telefones(@PathVariable("idpessoa") Long idpessoa) {

		ModelAndView andView = new ModelAndView("cadastro/telefones");

		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);		
		andView.addObject("pessoaobj", pessoa.get());
		andView.addObject("telefones", telefoneRepository.getTelefones(idpessoa));
				
		
		return andView;
	}
	
	
	
	/*
	 * REMOVER TELEFONE
	 * */
	@GetMapping("/removertelefone/{idtelefone}")
	public ModelAndView remover(@PathVariable("idtelefone") Long idtelefone) {
		
		Pessoa pessoa = telefoneRepository.findById(idtelefone).get().getPessoa();
		telefoneRepository.deleteById(idtelefone);

		ModelAndView andView = new ModelAndView("cadastro/telefones");
		andView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		andView.addObject("pessoaobj", pessoa);

		return andView;
	}
	
	
	/*
	 * EXCLUIR PESSOA
	 * */
	@GetMapping("/excluirpessoa/{idpessoa}")
	public ModelAndView excluir(@PathVariable("idpessoa") Long idpessoa) {
		
		pessoaRepository.deleteById(idpessoa);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());

		return andView;
	}


	/*
	 * LISTAR TODOS
	 * */
	@RequestMapping(method = RequestMethod.GET, value = "/listarpessoas")
	public ModelAndView findAll() {

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

		andView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		andView.addObject("pessoaobj", new Pessoa());
		andView.addObject("profissoes", profissaoRepository.findAll());
		
		return andView;

	}

}
