import { Component, OnInit, Inject } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource} from '@angular/material';
import {ActivatedRoute} from '@angular/router';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';

@Component({
  selector: 'app-active-org',
  templateUrl: './active-org.component.html',
  styleUrls: ['./active-org.component.css']
})
export class ActiveOrgComponent implements OnInit {

  constructor(private route: ActivatedRoute, public dialog: MatDialog) { }
  dataSource: any;
  displayedColumns: any;
  
  
  ngOnInit() {
    this.displayedColumns = ['name', 'domain', 'owner', 'description', 'provider', 'details'];
    this.dataSource =new MatTableDataSource<Element>(ELEMENT_DATA);
  }
  

  /**
   * Create New Organization
   */
  openDialog4CreateOrg(): void {
    let dialogRef = this.dialog.open(CreateOrganizationDialogComponent, {
      width: '40%',
      height:'60%',
      data: { name: "", dialogStatus:"TitleCreate"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed and result: ',result);
    });
  }

}
/**
 * Component for Dialog
 */
@Component({
  selector : 'create-org-dialog',
  templateUrl : 'create-org-dialog.html',
  styleUrls: ['./active-org.component.css']
})
export class CreateOrganizationDialogComponent {
  constructor(public dialogRef: MatDialogRef<CreateOrganizationDialogComponent>,@Inject(MAT_DIALOG_DATA) public data: any,  private _fb: FormBuilder) { }
  checkedProvider= false;
  orgNameControl = new FormControl('', [Validators.required]);
  ownerNameControl = new FormControl('', [Validators.required]);
  ownerEmailControl= new FormControl('', [Validators.required, Validators.email]);
  orgDescriptionControl= new FormControl('', [Validators.required]);
  orgDomainControl= new FormControl('', [Validators.required]);
  formDoc: FormGroup;
  ngOnInit() {
    this.formDoc = this._fb.group({
      basicfile: []
    });
  }
  onSubmit() {
    console.log('SUBMITTED', this.formDoc);
  }
  getErrorMessage4orgName() {
    return this.orgNameControl.hasError('required') ? 'You must enter a Name of the Organization.' :
        //this.dataset.hasError('email') ? 'Not a valid email' :
            '';
  }
  getErrorMessage4ownerName(){
    return this.ownerNameControl.hasError('required') ? 'You must enter a Name of the Owner.' :
        //this.dataset.hasError('email') ? 'Not a valid email' :
            '';
  }
  getErrorMessage4ownerEmail(){
    return this.ownerEmailControl.hasError('required') ? 'You must enter a valid email of the owner.' :
        this.ownerEmailControl.hasError('email') ? 'Not a valid email' :
            '';
  }
  getErrorMessage4orgDescription() {
    return this.orgNameControl.hasError('required') ? 'You must enter Description of the Organization.' :
        //this.dataset.hasError('email') ? 'Not a valid email' :
            '';
  }
  getErrorMessage4orgDomain() {
    return this.orgNameControl.hasError('required') ? 'You must enter a Domain of the Organization.' :
        //this.dataset.hasError('email') ? 'Not a valid email' :
            '';
  }
  onNoClick(): void {
    this.dialogRef.close();
  }
}

/*
* test table data
*/

export interface Element {
  id: number;
  name: string;
  domain: string;
  owner: string;
  description: string;
  provider: string;
}

const ELEMENT_DATA: Element[] = [
  {id: 1, name: 'test1', domain: 'project1', owner: 'test1@fbk.eu', description:'test description', provider:'yes'},
  {id: 2, name: 'test2', domain: 'project2', owner: 'test2@fbk.eu', description:'test description', provider:'no'},
  {id: 3, name: 'test3', domain: 'project3', owner: 'test3@fbk.eu', description:'test description', provider:'yes'},
  {id: 4, name: 'test4', domain: 'project4', owner: 'test4@fbk.eu', description:'test description', provider:'no'},
  {id: 5, name: 'test5', domain: 'project5', owner: 'test5@fbk.eu', description:'test description', provider:'yes'}
];