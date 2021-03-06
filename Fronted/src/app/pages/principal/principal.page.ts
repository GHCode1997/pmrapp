import { Component, OnInit, ViewChild, AfterViewInit, ElementRef, OnChanges, SimpleChanges } from '@angular/core';
import { ToolbarComponent } from 'src/app/components/toolbar/toolbar.component';
import { AuthService, TOKEN_NAME } from 'src/app/services/auth.service';
import { PacienteService } from 'src/app/services/paciente.service';
import { Paciente } from 'src/app/models/paciente';
import { Cesfam } from 'src/app/models/cesfam';
import * as jwt_decode from 'jwt-decode';
import { HttpHeaders } from '@angular/common/http';
@Component({
  selector: 'app-principal',
  templateUrl: './principal.page.html',
  styleUrls: ['./principal.page.scss'],
})
export class PrincipalPage implements OnInit, AfterViewInit, OnChanges {


  @ViewChild(ToolbarComponent, { static: true }) toolbarComponent: ToolbarComponent;

  public mode: string = 'over';

  public menu: Menu[] = [
    { path: 'buscador', title: 'Buscador de horas', icon: 'search' },
    { path: 'horas-solicitadas', title: 'Horas solicitadas', icon: 'event' }
  ];

  username: string = '';
  paciente: Paciente = {} as Paciente;


  constructor(private authService: AuthService,
    private pacienteService: PacienteService) {
    this.paciente.cesfam = {} as Cesfam;
  }

  ngOnInit() {
    var decoded = jwt_decode(localStorage.getItem(TOKEN_NAME));
    this.authService.currentUserSubject.next(decoded.sub);
    this.authService.httpOptions.headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + localStorage.getItem(TOKEN_NAME)
    })
    this.toolbarComponent.sidenav = {
      action: function (sidenav) {
        sidenav.opened = !sidenav.opened;
      }
    };
    this.authService.getCurrentUser()
      .subscribe(data => {
        this.pacienteService.getPacienteWithRun(data)
          .subscribe(data => {
            this.paciente = data;
          }, error => {
            console.error(error);
          })
      });
  }

  ngAfterViewInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {

  }



  cerrarSesion() {
    this.authService.authenticated = false;
    localStorage.setItem(TOKEN_NAME, '');
  }

}

export interface Menu {
  path: string;
  title: string;
  icon?: string;
}
