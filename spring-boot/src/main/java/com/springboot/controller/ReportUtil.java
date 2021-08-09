package com.springboot.controller;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.stereotype.Component;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Component
public class ReportUtil implements Serializable {
	
	private static final long serialVersionUID = 1L;

	
	//Retorna PDF em byte para download no navegador
	public byte[] gerarRelatorio(List listaDados, String relatorio, ServletContext servletContext) throws Exception{
		
		//Cria a lista de dados para o relatorio com nossa lista de objetos para imprimir
		JRBeanCollectionDataSource jrbcds = new JRBeanCollectionDataSource(listaDados);
		
		//Carregar o caminho do arquivo jasper compilado
		String caminhoJasper = servletContext.getRealPath("relatorios" + File.separator + relatorio + ".jasper");
		
		//Carrega o arquivo jasper passando os dados
		JasperPrint impressoraJasper = JasperFillManager.fillReport(caminhoJasper, new HashMap(), jrbcds);
		
		/*Exporta para byte para fazer o download do pdf*/
		return JasperExportManager.exportReportToPdf(impressoraJasper);
		
	}
	
}
