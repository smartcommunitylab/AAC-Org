import { Component, Injectable } from '@angular/core';
import { MatDialogRef, MatDialog } from '@angular/material';
import { Observable } from 'rxjs/Observable';

@Component({
    selector: 'app-alert-dialog',
    templateUrl: 'dialog-alert.component.html'
})
export class DialogAlertComponent {
    okBtn: string;
    message: string;
    title: string;

    constructor(
        public dialogRef: MatDialogRef<DialogAlertComponent>,
    ) {
    }

    confirm() {
        this.dialogRef.close();
    }
}
@Component({
    selector: 'app-confirm-dialog',
    templateUrl: 'dialog-confirm.component.html'
})
export class DialogConfirmComponent {
    okBtn = 'OK';
    cancelBtn = 'Cancel';
    message: string;
    title: string;

    constructor(
        public dialogRef: MatDialogRef<DialogConfirmComponent>,
    ) {
    }

    cancel() {
        this.dialogRef.close(false);
    }
    confirm() {
        this.dialogRef.close(true);
    }
}
@Injectable()
export class DialogService {

    constructor(
        private dialog: MatDialog
    ) {}

    alert(title: string, message: string, okButton?: string): Observable<void> {
        const dialogRef = this.dialog.open(DialogAlertComponent, {width: '40%'});
        dialogRef.componentInstance.message = message;
        dialogRef.componentInstance.okBtn = okButton || 'OK';
        dialogRef.componentInstance.title = title;
        return dialogRef.afterClosed();
    }
    confirm(title: string, message: string, okButton?: string, cancelButton?: string): Observable<boolean> {
        const dialogRef = this.dialog.open(DialogConfirmComponent, {width: '40%'});
        dialogRef.componentInstance.message = message;
        dialogRef.componentInstance.okBtn = okButton || 'OK';
        dialogRef.componentInstance.cancelBtn = cancelButton || 'Cancel';
        dialogRef.componentInstance.title = title;
        return dialogRef.afterClosed();
    }
}
