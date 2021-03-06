/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.ufro.dci.pmrteam.backendpmrapp.controllers;

import cl.ufro.dci.pmrteam.backendpmrapp.models.Especialidad;
import cl.ufro.dci.pmrteam.backendpmrapp.models.HoraEspecialista;
import cl.ufro.dci.pmrteam.backendpmrapp.models.Paciente;
import cl.ufro.dci.pmrteam.backendpmrapp.repositorys.EspecialidadRepository;
import cl.ufro.dci.pmrteam.backendpmrapp.repositorys.HoraEspecialistaRepository;
import cl.ufro.dci.pmrteam.backendpmrapp.repositorys.PacienteRepository;
import java.security.Principal;
import java.util.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import net.minidev.json.JSONObject;

/**
 *
 * @author Gustavo Huerta
 */
@RepositoryRestController
@RestController
@RequestMapping("hora")
public class HorasController {
    
    @Autowired
    private EspecialidadRepository especialidadRepository;
    
    @Autowired
    private HoraEspecialistaRepository horasRepository;
    
    @Autowired
    private PacienteRepository pacienteRepository;
    
    @GetMapping("byEspecialidad")
    public List<HoraEspecialista> indexAll (@RequestParam("nombre") String especialidad) {
        Optional<Especialidad> opEsp = this.especialidadRepository.findBynombre(especialidad);
        if(opEsp.isPresent()) {
            return this.horasRepository.findAllByespecialidadAndAsignada(opEsp.get(), false);
        }
        return null;
    }
    
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @GetMapping("byPaciente")
    public List<HoraEspecialista> indexHoras(Principal userAuth) {
        Optional<Paciente> op = Optional.of(this.pacienteRepository.findByrun(userAuth.getName()));
       Paciente pac = new Paciente();
        if(op.isPresent()){
             pac = op.get();
        }
        return this.horasRepository.findAllBypaciente(pac);
    }
    
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @PostMapping(value= "toPaciente")
    public ResponseEntity<HoraEspecialista> asignarPaciente(@RequestParam("id") Long id, @RequestBody JSONObject paciente, Principal user ) {

        Optional<HoraEspecialista> opHora = this.horasRepository.findById(id);
        Optional<Paciente> opPaciente = Optional.of(this.pacienteRepository.findByrun(user.getName()));
        Optional<List<HoraEspecialista>> oplisthora = Optional.of(this.horasRepository.findAllBypacienteAndFechaConsulta(opPaciente.get(),opHora.get().getFechaConsulta()));
        if(!oplisthora.get().isEmpty()) {
            return new ResponseEntity<>(null,HttpStatus.CONFLICT);
        }
        if (opHora.isPresent() && opPaciente.isPresent()  && opPaciente.get().getRun().equalsIgnoreCase(user.getName())) {
            HoraEspecialista horaEspecialista = opHora.get();
            horaEspecialista.setPaciente( opPaciente.get());
            horaEspecialista.setAsignada(true);
            horaEspecialista.setObservacion(paciente.getAsString("comment"));
            this.horasRepository.save(horaEspecialista);
            return new ResponseEntity<>(horaEspecialista,HttpStatus.CREATED) ;
        }
        
        return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
    }
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @PostMapping(value = "cancelar")
    public boolean cancelarPeticion(@RequestBody HoraEspecialista hora) {
        Optional<HoraEspecialista> horaOp = Optional.of(hora);
        if (horaOp.isPresent()) {
          horaOp= Optional.of(this.horasRepository.save(horaOp.get()));
          if(horaOp.isPresent()){
              return true;
          }
        }
        return false;
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value="realizada")
    public void realizada(@RequestParam("id_hora") Long id) throws Exception {
        Optional<HoraEspecialista> op = this.horasRepository.findById(id);
        if(op.isPresent()){
            HoraEspecialista hora = op.get();
            hora.setRealizada(true);
            this.horasRepository.save(hora);
        }else {
            throw  new Exception("No existe tal hora");
        }
    }
    
    @PreAuthorize("hasRole('ROLE_PACIENTE')")
    @PostMapping("updateImage")
    public ResponseEntity<Boolean> updatePicture(@RequestParam("id") Long id, @RequestBody JSONObject json){
        Optional<HoraEspecialista> op = this.horasRepository.findById(id);
        if(op.isPresent()){
            HoraEspecialista hora = op.get();
            System.out.println(json.getAsString("url"));
            hora.setQr(json.getAsString("url"));
            this.horasRepository.save(hora);
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    } 
}
