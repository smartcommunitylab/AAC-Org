import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-deactivate-org',
  templateUrl: './deactivate-org.component.html',
  styleUrls: ['./deactivate-org.component.css']
})
export class DeactivateOrgComponent implements OnInit {

  constructor(private route: ActivatedRoute) { }

  ngOnInit() {
  }

}
