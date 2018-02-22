import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveOrgComponent } from './active-org.component';

describe('ActiveOrgComponent', () => {
  let component: ActiveOrgComponent;
  let fixture: ComponentFixture<ActiveOrgComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ActiveOrgComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ActiveOrgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
