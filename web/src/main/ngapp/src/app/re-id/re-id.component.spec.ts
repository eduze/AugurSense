import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReIdComponent } from './re-id.component';

describe('ReIdComponent', () => {
  let component: ReIdComponent;
  let fixture: ComponentFixture<ReIdComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ReIdComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ReIdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
