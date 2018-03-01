import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonStopPointsComponent } from './person-stop-points.component';

describe('PersonStopPointsComponent', () => {
  let component: PersonStopPointsComponent;
  let fixture: ComponentFixture<PersonStopPointsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PersonStopPointsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonStopPointsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
