package com.springboot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.springboot.model.Pessoa;
import com.springboot.model.Telefone;
import com.springboot.repository.PessoaRepository;
import com.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;

	
	
	/*
	 * Chama pagina de CADASTRO DE PESSOA
	 * */
	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView inicio() {
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");		
		Iterable<Pessoa> pessoas = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoas);
		andView.addObject("pessoaobj", new Pessoa());
		
		return andView;
	}
	
	
	/*
	 * PESQUISAR PESSO POR NOME
	 * */
	@PostMapping("**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nomepesquisa) {
		
		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		andView.addObject("pessoas", pessoaRepository.findPessoaByName(nomepesquisa));
		andView.addObject("pessoaobj", new Pessoa());
		
		return andView;
	}


	/*
	 * SALVAR
	 * */
	@RequestMapping(method = RequestMethod.POST, value = "**/salvarpessoa") //** -> ignora tudo que vem antes
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult) {

		
		
		/*
		 * Valida erros do formulário
		 * Model Pessoa
		 * */
		if(bindingResult.hasErrors()) {
			
			ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
			Iterable<Pessoa> pessoas = pessoaRepository.findAll();
			modelAndView.addObject("pessoas", pessoas);
			modelAndView.addObject("pessoaobj", pessoa); //Mantem formulário preenchido
			
			List<String> msg = new ArrayList<String>();
			
			for(ObjectError objectError : bindingResult.getAllErrors()) {
				msg.add(objectError.getDefaultMessage()); //Vem das @anotações
			}
			
			modelAndView.addObject("msg", msg);
			
			return modelAndView;
		}
		
		pessoaRepository.save(pessoa);

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");
		Iterable<Pessoa> pessoas = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoas);
		andView.addObject("pessoaobj", new Pessoa());
		
		return andView;

	}


	/*
	 * EDITAR
	 * */
	@GetMapping("/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idpessoa) {

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

		Optional<Pessoa> pessoa = pessoaRepository.findById(idpessoa);		
		andView.addObject("pessoaobj", pessoa.get());

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
		
		//Número
		if(telefone != null && 
				(telefone.getNumero() != null && telefone.getNumero().isEmpty()) || 
				telefone.getNumero() == null) {			
			valid = false;
			msg.add("Número deve ser informado");			
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
	 * Chama página TELEFONES relacionado com PESSOA
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
		andView.addObject("pessoas", pessoaRepository.findAll());
		andView.addObject("pessoaobj", new Pessoa());

		return andView;
	}


	/*
	 * LISTAR TODOS
	 * */
	@RequestMapping(method = RequestMethod.GET, value = "/listarpessoas")
	public ModelAndView findAll() {

		ModelAndView andView = new ModelAndView("cadastro/cadastropessoa");

		Iterable<Pessoa> pessoas = pessoaRepository.findAll();
		andView.addObject("pessoas", pessoas);
		andView.addObject("pessoaobj", new Pessoa());
		
		return andView;

	}

}
