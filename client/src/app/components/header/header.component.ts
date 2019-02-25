import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router } from '@angular/router';
import { LoginService } from '../../services/auth/login.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  currentUser: any;
  constructor(private route: ActivatedRoute, private router: Router, private login: LoginService) { }

  ngOnInit() {
    this.login.getProfile().subscribe(profile => this.currentUser = profile);
  }

  logout() {
    this.login.logout();
  }
}
