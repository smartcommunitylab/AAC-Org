import { Component, OnInit, Inject } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatTableDataSource} from '@angular/material';
import {ActivatedRoute} from '@angular/router';
import {FormControl, Validators, FormBuilder, FormGroup} from '@angular/forms';
import {OrganizationService} from '../../services/organization.service';
import { OrganizationProfile, contentOrg } from '../../models/profile';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-active-org',
  templateUrl: './active-org.component.html',
  styleUrls: ['./active-org.component.css']
})
export class ActiveOrgComponent implements OnInit {

  constructor(private organizationService: OrganizationService, private route: ActivatedRoute, public dialog: MatDialog) { }
  orgProfile: OrganizationProfile[];
  orgActive: Array<OrganizationProfile>=[];
  contentOrg: contentOrg;
  dataSource: any;
  displayedColumns: any;

  
  ngOnInit() {
    this.dataSource ='';
    this.organizationService.getOrganizations().then(response => {
      for(var i=0; i<response["content"].length; i++){
        if(response["content"][i]["active"]){
          this.orgActive.push(response["content"][i]);
        }
      }
      this.orgProfile = response;
      this.displayedColumns = ['name', 'domain', 'owner', 'description', 'provider', 'details'];
      this.dataSource =new MatTableDataSource<OrganizationProfile>(this.orgActive);
    });

  }


  /**
   * Create New Organization
   */
  openDialog4CreateOrg(): void {
    let dialogRef = this.dialog.open(CreateOrganizationDialogComponent, {
      width: '40%',
      data: { org_name: "", org_domain:"", org_description:"", dialogStatus:"TitleCreateOrg"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result){
        this.organizationService.setOrganization(result).subscribe(
          res => {
            //for reload the table
            setTimeout(()=>{  this.ngOnInit();},1000);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            if(err.error){
              let dialogRefErr = this.dialog.open(CreateOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
              });
            }
            
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.',err);
            }
          }
        );
      }
    });
  }

  /**
   * Delete an Org
   * @param orgID 
   * @param orgName 
   */
  openDialog4ChangeStatusOrg(orgID, orgName): void {

    let dialogRef = this.dialog.open(CreateOrganizationDialogComponent, {
      width: '25%',
      data: { org_name: orgName, org_id:orgID, dialogStatus:"TitleChangeStatusOrg"  }
    });

    dialogRef.afterClosed().subscribe(result => {
      if(result){
        this.organizationService.disableOrganization(orgID,orgName).subscribe(
          res => {
            //for reload the table
            setTimeout(()=>{  this.ngOnInit();},1000);
          },
          (err: HttpErrorResponse) => {
            //open a error dialog with err.error
            if(err.error){
              let dialogRefErr = this.dialog.open(CreateOrganizationDialogComponent, {
                width: '30%',
                data: { error: err.error, dialogStatus:"TitleErrorMessage"  }
              });
            }
            if (err.error instanceof Error) {
              console.log('Client-side error occured.');
            } else {
              console.log('Server-side error occured.');
            }
          }
        );
        
      }
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
  ownerSurnameControl = new FormControl('', [Validators.required]);
  ownerEmailControl= new FormControl('', [Validators.required, Validators.email]);
  orgDescriptionControl= new FormControl('', [Validators.required]);
  orgDomainControl= new FormControl('');
  webAddressControl= new FormControl('');
  logoControl= new FormControl('');
  mobileControl= new FormControl('');
  tagControl= new FormControl('');
  statusControl= new FormControl(true);
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
    return this.orgNameControl.hasError('required') ? 'You must enter the name of the organization.' :
            '';
  }
  getErrorMessage4ownerName(){
    return this.ownerNameControl.hasError('required') ? 'Enter the name of the owner.' :
            '';
  }
  getErrorMessage4ownerSurname(){
    return this.ownerNameControl.hasError('required') ? 'Enter the surame of the owner.' : '';
  }
  getErrorMessage4ownerEmail(){
    return this.ownerEmailControl.hasError('required') ? 'You must enter the e-mail address of the owner.' :
        this.ownerEmailControl.hasError('email') ? 'Not a valid e-mail address.' :
            '';
  }
  getErrorMessage4orgDescription() {
    return this.orgNameControl.hasError('required') ? 'You must provide a description for the organization.' :
            '';
  }
  getErrorMessage4orgDomain() {
    return this.orgNameControl.hasError('required') ? 'You must enter the domain of the organization.' :
            '';
  }
  onNoClick(): void {
    this.dialogRef.close();
  }
}